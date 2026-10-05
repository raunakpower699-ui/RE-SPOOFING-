package com.example.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Non-Stop Unlimited Foreground Service ("Runs until exited from Notification Panel").
 * - Uses START_STICKY + android:stopWithTask="false" + AlarmManager self-revival on task removal.
 * - Continuously updates the ongoing notification every 5 seconds with live 96%–100% CPU/GPU telemetry.
 * - Only stops when the user explicitly taps EXIT / STOP in the Notification Panel or STOP inside the app.
 */
class PerformanceForegroundService : Service() {

    private val engine by lazy { AndroidPerformanceEngine.getInstance(applicationContext) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var notificationHeartbeatJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_PERFORMANCE -> {
                savePersistentSessionState(this, active = false, null, null, null, null)
                stopPerformanceSessionAndService("User exited from notification panel / STOP button")
                return START_NOT_STICKY
            }
            ACTION_START_PERFORMANCE -> {
                val profileId = intent.getStringExtra(EXTRA_PROFILE_ID)
                val focusId = intent.getStringExtra(EXTRA_WORKLOAD_FOCUS_ID)
                val gamePkg = intent.getStringExtra(EXTRA_GAME_PACKAGE)
                val gameName = intent.getStringExtra(EXTRA_GAME_NAME)

                val profile = PerformanceProfile.fromId(profileId)
                val focus = WorkloadFocus.fromId(focusId)

                savePersistentSessionState(
                    context = this,
                    active = true,
                    profileId = profile.id,
                    focusId = focus.id,
                    gamePkg = gamePkg,
                    gameName = gameName
                )

                val notification = buildPersistentNotification(profile, focus, gameName)
                startForegroundSafely(notification)

                engine.startPerformanceSession(
                    profile = profile,
                    workloadFocus = focus,
                    associatedGamePackage = gamePkg,
                    associatedGameName = gameName
                )
                startNotificationHeartbeatLoop()
            }
            else -> {
                // START_STICKY restart or watchdog revival after OS memory pressure
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val shouldStayActive = prefs.getBoolean(KEY_PERSISTENT_ACTIVE, false)
                val current = engine.telemetryState.value
                if (current.isSessionActive || shouldStayActive) {
                    val savedProfile = PerformanceProfile.fromId(
                        prefs.getString(KEY_PROFILE_ID, current.selectedProfile.id)
                    )
                    val savedFocus = WorkloadFocus.fromId(
                        prefs.getString(KEY_FOCUS_ID, current.selectedWorkloadFocus.id)
                    )
                    val savedGamePkg = prefs.getString(KEY_GAME_PKG, current.activeGamePackage)
                    val savedGameName = prefs.getString(KEY_GAME_NAME, current.activeGameName)

                    val notification = buildPersistentNotification(savedProfile, savedFocus, savedGameName)
                    startForegroundSafely(notification)
                    if (!current.isSessionActive) {
                        engine.startPerformanceSession(
                            profile = savedProfile,
                            workloadFocus = savedFocus,
                            associatedGamePackage = savedGamePkg,
                            associatedGameName = savedGameName
                        )
                    }
                    startNotificationHeartbeatLoop()
                } else {
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    private fun startNotificationHeartbeatLoop() {
        if (notificationHeartbeatJob?.isActive == true) return
        notificationHeartbeatJob = serviceScope.launch {
            while (isActive) {
                delay(5000L)
                val st = engine.telemetryState.value
                if (!st.isSessionActive) break
                try {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    if (nm != null) {
                        val updated = buildPersistentNotification(
                            profile = st.selectedProfile,
                            focus = st.selectedWorkloadFocus,
                            gameName = st.activeGameName
                        )
                        nm.notify(NOTIFICATION_ID, updated)
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun startForegroundSafely(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Throwable) {
        }
    }

    private fun stopPerformanceSessionAndService(reason: String) {
        notificationHeartbeatJob?.cancel()
        notificationHeartbeatJob = null
        engine.stopPerformanceSession(reason)
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Throwable) {
        }
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep running non-stop even if user swipes the app away from Recents while gaming!
        // Only stop when user explicitly presses EXIT / STOP in the notification panel.
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shouldStayActive = prefs.getBoolean(KEY_PERSISTENT_ACTIVE, false)
        if (shouldStayActive || engine.telemetryState.value.isSessionActive) {
            scheduleSelfRevivalAlarm()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        notificationHeartbeatJob?.cancel()
        notificationHeartbeatJob = null
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val shouldStayActive = prefs.getBoolean(KEY_PERSISTENT_ACTIVE, false)
        if (shouldStayActive) {
            // Unexpected OS kill — schedule immediate revival so it never dies after 10-15 minutes!
            scheduleSelfRevivalAlarm()
        } else if (engine.telemetryState.value.isSessionActive) {
            engine.stopPerformanceSession("Foreground service stopped")
        }
        super.onDestroy()
    }

    private fun scheduleSelfRevivalAlarm() {
        try {
            val am = getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val st = engine.telemetryState.value
            val reviveIntent = Intent(applicationContext, PerformanceForegroundService::class.java).apply {
                action = ACTION_START_PERFORMANCE
                putExtra(EXTRA_PROFILE_ID, st.selectedProfile.id)
                putExtra(EXTRA_FOCUS_ID_FALLBACK, st.selectedWorkloadFocus.id)
                putExtra(EXTRA_WORKLOAD_FOCUS_ID, st.selectedWorkloadFocus.id)
                putExtra(EXTRA_GAME_PACKAGE, st.activeGamePackage)
                putExtra(EXTRA_GAME_NAME, st.activeGameName)
            }
            val pi = PendingIntent.getService(
                applicationContext,
                777,
                reviveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = SystemClock.elapsedRealtime() + 1200L
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi)
        } catch (_: Throwable) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        gameName: String?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PerformanceForegroundService::class.java).apply {
            action = ACTION_STOP_PERFORMANCE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            101,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val st = engine.telemetryState.value
        val cpuPct = st.lockedPowerPercent.coerceIn(96, 100)
        val gpuPct = st.gpuLockedDutyPercent.coerceIn(96, 100)
        val hrs = st.sessionElapsedSeconds / 3600
        val mins = (st.sessionElapsedSeconds % 3600) / 60
        val secs = st.sessionElapsedSeconds % 60
        val timeStr = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

        val subtitle = "CPU: $cpuPct% LOCKED • GPU: $gpuPct% LOCKED • Run: $timeStr"
        val detailLine = buildString {
            append(subtitle)
            append("\nMode: ${profile.title} (${focus.title}) • Non-Stop Unlimited Lock Active")
            if (!gameName.isNullOrBlank()) {
                append(" • Game: $gameName")
            }
            append("\nTap EXIT / STOP below to stop RE Spoofing.")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${getString(R.string.notif_title)} ($cpuPct% MAX)")
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailLine))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_delete,
                "EXIT / STOP",
                stopPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                getString(R.string.notif_action_open),
                openAppPendingIntent
            )
            .build()
    }

    companion object {
        const val CHANNEL_ID = "vivo_spoofing_perf_channel"
        const val NOTIFICATION_ID = 4096

        private const val PREFS_NAME = "re_spoofing_fgs_state"
        private const val KEY_PERSISTENT_ACTIVE = "key_persistent_active"
        private const val KEY_PROFILE_ID = "key_profile_id"
        private const val KEY_FOCUS_ID = "key_focus_id"
        private const val KEY_GAME_PKG = "key_game_pkg"
        private const val KEY_GAME_NAME = "key_game_name"

        const val ACTION_START_PERFORMANCE = "com.example.action.START_PERFORMANCE"
        const val ACTION_STOP_PERFORMANCE = "com.example.action.STOP_PERFORMANCE"

        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_WORKLOAD_FOCUS_ID = "extra_workload_focus_id"
        private const val EXTRA_FOCUS_ID_FALLBACK = "extra_focus_id_fallback"
        const val EXTRA_GAME_PACKAGE = "extra_game_package"
        const val EXTRA_GAME_NAME = "extra_game_name"

        private fun savePersistentSessionState(
            context: Context,
            active: Boolean,
            profileId: String?,
            focusId: String?,
            gamePkg: String?,
            gameName: String?
        ) {
            try {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_PERSISTENT_ACTIVE, active)
                    .apply {
                        if (profileId != null) putString(KEY_PROFILE_ID, profileId)
                        if (focusId != null) putString(KEY_FOCUS_ID, focusId)
                        putString(KEY_GAME_PKG, gamePkg)
                        putString(KEY_GAME_NAME, gameName)
                    }
                    .apply()
            } catch (_: Throwable) {
            }
        }

        fun startServiceSession(
            context: Context,
            profile: PerformanceProfile,
            workloadFocus: WorkloadFocus,
            gamePackage: String? = null,
            gameName: String? = null
        ) {
            val engine = AndroidPerformanceEngine.getInstance(context)
            val started = engine.startPerformanceSession(
                profile = profile,
                workloadFocus = workloadFocus,
                associatedGamePackage = gamePackage,
                associatedGameName = gameName
            )
            if (!started) return

            savePersistentSessionState(
                context = context,
                active = true,
                profileId = profile.id,
                focusId = workloadFocus.id,
                gamePkg = gamePackage,
                gameName = gameName
            )

            val intent = Intent(context, PerformanceForegroundService::class.java).apply {
                action = ACTION_START_PERFORMANCE
                putExtra(EXTRA_PROFILE_ID, profile.id)
                putExtra(EXTRA_WORKLOAD_FOCUS_ID, workloadFocus.id)
                putExtra(EXTRA_GAME_PACKAGE, gamePackage)
                putExtra(EXTRA_GAME_NAME, gameName)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Throwable) {
            }
        }

        fun stopServiceSession(context: Context, reason: String = "User pressed STOP") {
            savePersistentSessionState(context, active = false, null, null, null, null)
            val engine = AndroidPerformanceEngine.getInstance(context)
            engine.stopPerformanceSession(reason)
            try {
                val intent = Intent(context, PerformanceForegroundService::class.java)
                context.stopService(intent)
            } catch (_: Throwable) {
            }
        }
    }
}
