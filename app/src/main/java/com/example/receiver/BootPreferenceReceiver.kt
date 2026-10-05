package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.engine.AndroidPerformanceEngine

/**
 * Boot receiver complying strictly with Section 8:
 * - Does NOT automatically activate maximum performance after device boot.
 * - Restores only the user's saved profile preference in standby state and requires
 *   explicit user interaction (pressing START in the UI) before activating performance mode.
 */
class BootPreferenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val engine = AndroidPerformanceEngine.getInstance(context.applicationContext)
            // Ensure session remains strictly INACTIVE on boot
            engine.refreshStaticAndDynamicTelemetry()
        }
    }
}
