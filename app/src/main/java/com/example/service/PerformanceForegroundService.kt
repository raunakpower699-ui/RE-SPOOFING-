package com.example.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PerformanceForegroundService : Service() {

    private val engine by lazy { AndroidPerformanceEngine.getInstance(applicationContext) }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var notificationHeartbeatJob: Job? = null
    private val isExplicitlyStopped = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_PERFORMANCE -> {
                isExplicitlyStopped.set(true)
                savePersistentSessionState(this, active = false, null, null, null, null)
                stopPerformanceSessionAndService("User exited from notification / STOP button")
                return START_NOT_STICKY
            }
            ACTION_START_PERFORMANCE -> {
                isExplicitlyStopped.set(false)
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
                if (isExplicitlyStopped.get()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val shouldStayActive = prefs.getBoolean(KEY_PERSISTENT_ACTIVE, false)
                val current = engine.telemetryState.value
                if ((current.isSessionActive || shouldStayActive) && !isExplicitlyStopped.get()) {
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
                if (!st.isSessionActive || isExplicitlyStopped.get()) break
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
            startForeground(NOTIFICATION_ID, notification)
        } catch (_: Throwable) {
        }
    }

    private fun stopPerformanceSessionAndService(reason: String) {
        notificationHeartbeatJob?.cancel()
        notificationHeartbeatJob = null
        engine.stopPerformanceSession(reason)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Throwable) {
        }
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(NOTIFICATION_ID)
        } catch (_: Throwable) {
        }
        stopSelf()
    }

    override fun onDestroy() {
        notificationHeartbeatJob?.cancel()
        notificationHeartbeatJob = null
        if (isExplicitlyStopped.get()) {
            super.onDestroy()
            return
        }
        if (engine.telemetryState.value.isSessionActive) {
            engine.stopPerformanceSession("Foreground service destroyed")
        }
        super.onDestroy()
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
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        gameName: String?
    ): Notification {
        val st = engine.telemetryState.value
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

        val targetName = gameName ?: "Continuous Lock Pipeline"
        val duty = st.lockedPowerPercent

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${profile.title} Active • $duty% Lock")
            .setContentText("Target: $targetName • CPU & GPU 100% Sustained")
            .setOngoing(true)
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
        const val CHANNEL_ID = "re_spoofing_perf_channel"
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
                val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_PERSISTENT_ACTIVE, active)
                if (profileId != null) editor.putString(KEY_PROFILE_ID, profileId)
                if (focusId != null) editor.putString(KEY_FOCUS_ID, focusId)
                editor.putString(KEY_GAME_PKG, gamePkg)
                editor.putString(KEY_GAME_NAME, gameName)
                editor.commit()
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
            val intent = Intent(context, PerformanceForegroundService::class.java).apply {
                action = ACTION_START_PERFORMANCE
                putExtra(EXTRA_PROFILE_ID, profile.id)
                putExtra(EXTRA_WORKLOAD_FOCUS_ID, workloadFocus.id)
                putExtra(EXTRA_GAME_PACKAGE, gamePackage)
                putExtra(EXTRA_GAME_NAME, gameName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopServiceSession(context: Context) {
            val intent = Intent(context, PerformanceForegroundService::class.java).apply {
                action = ACTION_STOP_PERFORMANCE
            }
            context.startService(intent)
        }
    }
}
