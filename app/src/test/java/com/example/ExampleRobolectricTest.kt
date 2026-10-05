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
    fun `verify RE Spoofing app identity and normal launch without intro video`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("RE Spoofing", context.getString(R.string.app_name))
        assertEquals("Maximum Available Performance Mode", context.getString(R.string.main_purpose))
        assertEquals("Thermal Protection: ACTIVE", context.getString(R.string.thermal_protection_active))
    }

    @Test
    fun `verify live phone specs, Diablo Mode, and all 8 real max hardware subsystems`() {
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

        // Unlock Vivo/iQOO verification and activate all max hardware subsystems
        engine.setVivoIqooEmulatorSimulation(true)
        engine.activateAllMaxHardwareSubsystems()
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
        assertTrue(diabloState.memoryBandwidthPrefetchEnabled)
        assertTrue(diabloState.lowLatencyAudioDspLockEnabled)
        assertTrue(diabloState.touchSensorBoostEnabled)
        assertTrue(diabloState.storageIoBoostEnabled)
        assertTrue(diabloState.minimalPostProcessingDisplayEnabled)
        assertTrue(diabloState.lockedPowerPercent in 97..100)
        assertTrue(diabloState.gpuLockedDutyPercent in 97..100)
        assertTrue(diabloState.memoryBandwidthMbPerSec >= 11000)
        assertTrue(diabloState.crc32AluOpsPerSecMillions >= 700)
        assertTrue(diabloState.sensorSamplingHz >= 120)
        assertTrue(diabloState.storageThroughputMbPerSec >= 1700)
        assertTrue(diabloState.audioFastPathActive)
        assertFalse(diabloState.sustainedModeRequestedOnWindow)
        assertEquals(1_600_000L, diabloState.cpuStatus.targetDurationNanos)
        assertEquals("ACTIVE (DIABLO OVERDRIVE)", diabloState.cpuStatus.requestState)

        // Verify toggling No-Touch Hardware Lock OFF immediately drops forced CPU & GPU duty
        engine.setNoTouchPowerLockEnabled(false)
        assertFalse(engine.telemetryState.value.noTouchPowerLockEnabled)
        assertEquals(0, engine.telemetryState.value.realMeasuredThreadDutyPercent)
        assertEquals(0, engine.telemetryState.value.gpuLockedDutyPercent)
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
        assertEquals(0, stoppedState.gpuLockedDutyPercent)
        assertEquals(0, stoppedState.memoryBandwidthMbPerSec)
        assertEquals(0, stoppedState.crc32AluOpsPerSecMillions)
        assertEquals(0, stoppedState.sensorSamplingHz)
        assertEquals(0, stoppedState.storageThroughputMbPerSec)
        assertEquals("INACTIVE", stoppedState.cpuStatus.requestState)
        assertEquals("INACTIVE", stoppedState.gpuStatus.requestState)
    }
}
