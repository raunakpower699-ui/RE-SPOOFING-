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
import com.example.MainActivity
import com.example.engine.AndroidPerformanceEngine

class PerformanceForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_PERFORMANCE) {
            AndroidPerformanceEngine.getInstance(applicationContext).stopPerformanceSession("Stopped via notification")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val engine = AndroidPerformanceEngine.getInstance(applicationContext)
        val state = engine.telemetryState.value

        val notification = buildPerformanceNotification(
            context = this,
            profileTitle = state.selectedProfile.title,
            powerPct = if (state.isSessionActive) state.lockedPowerPercent else 100,
            lockedHz = state.selectedRefreshRate.hz,
            cpuTemp = state.thermalStatus.cpuTempCelsius,
            gpuTemp = state.thermalStatus.gpuTempCelsius,
            isThrottling = state.thermalStatus.isThrottling
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    companion object {
        const val CHANNEL_ID = "channel_performance_engine"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_PERFORMANCE = "com.example.service.ACTION_STOP_PERFORMANCE"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Performance Engine Service",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Keeps CPU/GPU clock floors locked in background for games and benchmarks."
                        setShowBadge(false)
                    }
                    nm.createNotificationChannel(channel)
                }
            }
        }

        fun buildPerformanceNotification(
            context: Context,
            profileTitle: String,
            powerPct: Int,
            lockedHz: Int,
            cpuTemp: Float,
            gpuTemp: Float,
            isThrottling: Boolean
        ): Notification {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopIntent = Intent(context, PerformanceForegroundService::class.java).apply {
                action = ACTION_STOP_PERFORMANCE
            }
            val stopPendingIntent = PendingIntent.getService(
                context,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isThrottling) {
                "⚠️ THERMAL THROTTLE ALERT • CPU ${cpuTemp}°C / GPU ${gpuTemp}°C"
            } else {
                "Raunak Exploits • $profileTitle ($powerPct% • ${lockedHz}Hz)"
            }
            val body = if (isThrottling) {
                "SoC heat is causing FPS drops • Cool down device (Zero-Contention Active)"
            } else {
                "${lockedHz}Hz Pinned • CPU ${cpuTemp}°C • GPU ${gpuTemp}°C • Game Anti-Drop ON"
            }

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(openPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Lock", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        }

        fun startService(context: Context) {
            try {
                val intent = Intent(context, PerformanceForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Throwable) {
            }
        }

        fun updateNotification(context: Context) {
            try {
                val engine = AndroidPerformanceEngine.getInstance(context.applicationContext)
                val state = engine.telemetryState.value
                if (!state.isSessionActive) return
                ensureNotificationChannel(context)
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                val notification = buildPerformanceNotification(
                    context = context,
                    profileTitle = state.selectedProfile.title,
                    powerPct = state.lockedPowerPercent,
                    lockedHz = state.selectedRefreshRate.hz,
                    cpuTemp = state.thermalStatus.cpuTempCelsius,
                    gpuTemp = state.thermalStatus.gpuTempCelsius,
                    isThrottling = state.thermalStatus.isThrottling
                )
                nm.notify(NOTIFICATION_ID, notification)
            } catch (_: Throwable) {
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, PerformanceForegroundService::class.java)
                context.stopService(intent)
            } catch (_: Throwable) {
            }
        }
    }
}
