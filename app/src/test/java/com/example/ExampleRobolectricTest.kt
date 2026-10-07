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

        // Unlock Vivo/iQOO verification and activate all max hardware subsystems (OriginOS 6 Dynamic Per-App 0.5x Scale Overdrive)
        engine.setVivoIqooEmulatorSimulation(true)
        engine.activateAllMaxHardwareSubsystems()
        val startedOriginResult = engine.startPerformanceSession(
            profile = PerformanceProfile.ORIGINOS_6_OVERDRIVE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertTrue(startedOriginResult)
        val originState = engine.telemetryState.value
        assertTrue(originState.isSessionActive)
        assertTrue(originState.originOs6OverdriveEnabled)
        assertTrue(originState.extremeRenderOverdriveEnabled)
        assertTrue(originState.dynamicPerAppScaleEnabled)
        assertTrue(originState.autoRestoreOnMinimizeEnabled)
        assertTrue(originState.isTargetWindowHookActive)
        assertTrue(originState.globalDisplayDpiLabel.contains("UNTOUCHED"))
        assertTrue(originState.renderScaleSpoofEnabled)
        assertEquals(0.50f, originState.renderScaleFactor, 0.001f)
        assertTrue(originState.internalShaderResolutionLabel.contains("540 x 1200"))
        assertTrue(originState.vSyncDisabledEglSwapZero)
        assertTrue(originState.gpuFlopOverdriveGflops >= 1400)
        assertTrue(originState.vivoPemThermalDaemonSuppressed)
        assertEquals(144, originState.targetFrameRateFps)
        assertEquals(64, originState.volumeShaderRayStepsPerFrame)
        assertEquals(
            "TARGET_HOOK_ACTIVE | RESOLUTION_SCALE: 0.5x (APP_ONLY) | GLOBAL_DPI: UNTOUCHED",
            originState.originOsOutputStatus
        )
        assertEquals(
            "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100%",
            originState.secondaryDirectiveStatus
        )
        assertTrue(originState.targetPipelineProcess.contains("com.volumeshader"))
        assertTrue(originState.targetPipelineProcess.contains("Mandelbulb"))
        assertEquals(1_400_000L, originState.cpuStatus.targetDurationNanos)
        assertEquals("ACTIVE (ORIGINOS6 OVERDRIVE)", originState.cpuStatus.requestState)

        // Verify AUTO_RESTORE_PROTOCOL: Minimizing / pressing Home immediately restores 1.0x (1080p native)
        engine.setTargetWindowHookActive(false, "Home Button pressed")
        val minimizedState = engine.telemetryState.value
        assertFalse(minimizedState.isTargetWindowHookActive)
        assertEquals(1.00f, minimizedState.renderScaleFactor, 0.001f)
        assertTrue(minimizedState.internalShaderResolutionLabel.contains("1080 x 2400"))
        assertEquals(
            "AUTO_RESTORE_1.0X_NATIVE | RESOLUTION_SCALE: 1.0x (1080p) | GLOBAL_DPI: UNTOUCHED",
            minimizedState.originOsOutputStatus
        )

        // Returning to target window restores 0.5x (APP_ONLY) scale
        engine.setTargetWindowHookActive(true, "Target window foreground")
        assertEquals(0.50f, engine.telemetryState.value.renderScaleFactor, 0.001f)
        assertEquals(
            "TARGET_HOOK_ACTIVE | RESOLUTION_SCALE: 0.5x (APP_ONLY) | GLOBAL_DPI: UNTOUCHED",
            engine.telemetryState.value.originOsOutputStatus
        )

        // Also verify DIABLO_MODE profile
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
