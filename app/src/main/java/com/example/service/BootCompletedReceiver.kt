package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppDatabase.getInstance(appContext).performanceDao().getUserPreferences().firstOrNull()
                if (prefs?.autoRestoreOnBoot == true) {
                    val engine = AndroidPerformanceEngine.getInstance(appContext)
                    val started = engine.startPerformanceSession(profile = PerformanceProfile.DIABLO_MODE)
                    if (started) {
                        try {
                            PerformanceForegroundService.startService(appContext)
                        } catch (_: Throwable) {
                        }
                    }
                }
            } catch (_: Throwable) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
