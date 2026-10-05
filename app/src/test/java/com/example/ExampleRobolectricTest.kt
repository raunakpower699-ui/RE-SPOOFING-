package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify RE Spoofing app identity and branding strings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("RE Spoofing", context.getString(R.string.app_name))
        assertEquals("Maximum Available Performance Mode", context.getString(R.string.main_purpose))
        assertEquals("Thermal Protection: ACTIVE", context.getString(R.string.thermal_protection_active))
    }

    @Test
    fun `verify live phone specs detection, No-Touch 97-100 lock, and Diablo Mode overdrive`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = AndroidPerformanceEngine.getInstance(context)
        engine.setVivoIqooEmulatorSimulation(false)
        engine.stopPerformanceSession("Reset before test")

        // Verify live phone specs are populated
        val specs = engine.readDeviceLiveSpecs()
        assertTrue(specs.phoneDisplayName.isNotBlank())
        assertTrue(specs.androidVersionLabel.contains("Android"))
        assertTrue(specs.cpuCores >= 1)

        // On non-Vivo/iQOO device, START must be blocked
        val blockedResult = engine.startPerformanceSession(
            profile = PerformanceProfile.DIABLO_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertFalse(blockedResult)
        assertFalse(engine.telemetryState.value.isSessionActive)
        assertEquals("LOCKED (VIVO/iQOO ONLY)", engine.telemetryState.value.cpuStatus.requestState)

        // When Vivo/iQOO device environment is verified, DIABLO MODE starts with 1.8ms overdrive and 97%-100% power lock
        engine.setVivoIqooEmulatorSimulation(true)
        val startedResult = engine.startPerformanceSession(
            profile = PerformanceProfile.DIABLO_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertTrue(startedResult)
        val diabloState = engine.telemetryState.value
        assertTrue(diabloState.isSessionActive)
        assertTrue(diabloState.isDiabloModeActive)
        assertTrue(diabloState.noTouchPowerLockEnabled)
        assertTrue(diabloState.antiThrottleBoosterEnabled)
        assertTrue(diabloState.lockedPowerPercent in 97..100)
        assertFalse(diabloState.sustainedModeRequestedOnWindow)
        assertEquals(1_800_000L, diabloState.cpuStatus.targetDurationNanos)
        assertEquals("ACTIVE (DIABLO OVERDRIVE)", diabloState.cpuStatus.requestState)

        // Verify toggling No-Touch Hardware Lock OFF immediately drops forced duty
        engine.setNoTouchPowerLockEnabled(false)
        assertFalse(engine.telemetryState.value.noTouchPowerLockEnabled)
        assertEquals(0, engine.telemetryState.value.realMeasuredThreadDutyPercent)
        engine.setNoTouchPowerLockEnabled(true)
        assertTrue(engine.telemetryState.value.noTouchPowerLockEnabled)

        // Verify background process purger executes cleanly
        val (purgedCount, _) = engine.purgeBackgroundProcessesAndBoostRam()
        assertTrue(purgedCount >= 0)

        // Stop session and verify cleanup
        engine.stopPerformanceSession("Unit test stop")
        val stoppedState = engine.telemetryState.value
        assertFalse(stoppedState.isSessionActive)
        assertFalse(stoppedState.isDiabloModeActive)
        assertEquals(0, stoppedState.lockedPowerPercent)
        assertEquals("INACTIVE", stoppedState.cpuStatus.requestState)
        assertEquals("INACTIVE", stoppedState.gpuStatus.requestState)
    }
}
