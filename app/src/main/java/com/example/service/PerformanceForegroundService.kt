package com.example.service

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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus

/**
 * Legitimate Foreground Service that manages the ongoing performance optimization session.
 * Displays a persistent, non-deceptive notification with STOP and OPEN APP actions (Section 6 & 7).
 */
class PerformanceForegroundService : Service() {

    private val engine by lazy { AndroidPerformanceEngine.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_PERFORMANCE -> {
                stopPerformanceSessionAndService("Stopped from notification action")
                return START_NOT_STICKY
            }
            ACTION_START_PERFORMANCE -> {
                val profileId = intent.getStringExtra(EXTRA_PROFILE_ID)
                val focusId = intent.getStringExtra(EXTRA_WORKLOAD_FOCUS_ID)
                val gamePkg = intent.getStringExtra(EXTRA_GAME_PACKAGE)
                val gameName = intent.getStringExtra(EXTRA_GAME_NAME)

                val profile = PerformanceProfile.fromId(profileId)
                val focus = WorkloadFocus.fromId(focusId)

                val notification = buildPersistentNotification(profile, focus, gameName)
                startForegroundSafely(notification)

                engine.startPerformanceSession(
                    profile = profile,
                    workloadFocus = focus,
                    associatedGamePackage = gamePkg,
                    associatedGameName = gameName
                )
            }
            else -> {
                val current = engine.telemetryState.value
                if (current.isSessionActive) {
                    val notification = buildPersistentNotification(
                        current.selectedProfile,
                        current.selectedWorkloadFocus,
                        current.activeGameName
                    )
                    startForegroundSafely(notification)
                } else {
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
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
            // Fallback for test environments or restricted OEM foreground states
        }
    }

    private fun stopPerformanceSessionAndService(reason: String) {
        engine.stopPerformanceSession(reason)
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Throwable) {
        }
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Section 7: If the application process is killed, make sure the system can safely clean up resources.
        stopPerformanceSessionAndService("App task removed — resources safely released")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
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

        val subtitle = getString(R.string.notif_subtitle)
        val detailLine = buildString {
            append(subtitle)
            append(" • Profile: ${profile.title} (${focus.title})")
            if (!gameName.isNullOrBlank()) {
                append(" • Target: $gameName")
            }
            append(" • Thermal Protection: ACTIVE")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailLine))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.notif_action_stop),
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

        const val ACTION_START_PERFORMANCE = "com.example.action.START_PERFORMANCE"
        const val ACTION_STOP_PERFORMANCE = "com.example.action.STOP_PERFORMANCE"

        const val EXTRA_PROFILE_ID = "extra_profile_id"
        const val EXTRA_WORKLOAD_FOCUS_ID = "extra_workload_focus_id"
        const val EXTRA_GAME_PACKAGE = "extra_game_package"
        const val EXTRA_GAME_NAME = "extra_game_name"

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
                // Engine session is already active even if OS restricts background service launch
            }
        }

        fun stopServiceSession(context: Context, reason: String = "User pressed STOP") {
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
