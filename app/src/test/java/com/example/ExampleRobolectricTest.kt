package com.example

import android.content.Context
import org.robolectric.RuntimeEnvironment
import com.example.engine.AndroidPerformanceEngine
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var engine: AndroidPerformanceEngine

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        engine = AndroidPerformanceEngine.getInstance(context)
        engine.isRobolectricTest = true
        engine.enforceVivoIqooOnlyLock = false
    }

    @Test
    fun verifyLivePhoneSpecsAndDiabloMode100PercentLock() {
        val specs = engine.readDeviceLiveSpecs()
        assertTrue(specs.phoneDisplayName.isNotBlank())
        assertTrue(specs.androidVersionLabel.contains("Android"))

        val started = engine.startPerformanceSession(
            profile = PerformanceProfile.DIABLO_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertTrue(started)

        val activeState = engine.telemetryState.value
        assertTrue(activeState.isSessionActive)
        assertEquals(100, activeState.lockedPowerPercent)
        assertEquals(100, activeState.gpuLockedDutyPercent)
        assertEquals(100, activeState.realMeasuredThreadDutyPercent)
        assertEquals("ACTIVE (DIABLO OVERDRIVE)", activeState.cpuStatus.requestState)

        engine.stopPerformanceSession("Test complete")
        val stoppedState = engine.telemetryState.value
        assertFalse(stoppedState.isSessionActive)
        assertEquals(0, stoppedState.lockedPowerPercent)
        assertEquals(0, stoppedState.gpuLockedDutyPercent)
        assertEquals(0, stoppedState.realMeasuredThreadDutyPercent)
    }

    @Test
    fun verifyBalanceAndSavingModes() {
        engine.startPerformanceSession(
            profile = PerformanceProfile.BALANCE_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertEquals(70, engine.telemetryState.value.lockedPowerPercent)
        assertEquals(70, engine.telemetryState.value.gpuLockedDutyPercent)

        engine.startPerformanceSession(
            profile = PerformanceProfile.SAVING_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        assertEquals(25, engine.telemetryState.value.lockedPowerPercent)
        assertEquals(25, engine.telemetryState.value.gpuLockedDutyPercent)

        engine.stopPerformanceSession("Done")
        assertEquals(0, engine.telemetryState.value.lockedPowerPercent)
    }

    @Test
    fun verifyUniversalDeviceTuningAndRamClean() {
        val specs = engine.readDeviceLiveSpecs()
        assertTrue(specs.universalCompatibilityCertified)
        assertTrue(specs.totalRamGb > 0f)

        engine.setUniversalTier(com.example.model.UniversalDeviceTier.LOW_END_BUDGET)
        assertEquals(com.example.model.UniversalDeviceTier.LOW_END_BUDGET, engine.telemetryState.value.activeUniversalTier)

        val freed = engine.optimizeAndCleanRam()
        assertTrue(freed > 0)
        assertEquals(freed, engine.telemetryState.value.freedRamMbLastClean)
    }

    @Test
    fun verifyRefreshRateLockAnd1PercentBatteryBypass() {
        engine.testBatteryOverridePercent = 1
        engine.refreshStaticAndDynamicTelemetry()
        assertEquals(1, engine.telemetryState.value.batteryStatus.levelPercent)

        for (target in com.example.model.RefreshRateTarget.entries) {
            engine.setRefreshRateTarget(target)
            engine.startPerformanceSession(
                profile = com.example.model.PerformanceProfile.DIABLO_MODE,
                workloadFocus = com.example.model.WorkloadFocus.COMBINED_MAX
            )
            assertEquals(100, engine.telemetryState.value.lockedPowerPercent)
            assertEquals(target.hz, engine.telemetryState.value.lockedRefreshRateHz)
            assertEquals(target.hz, engine.telemetryState.value.liveMeasuredFps)
            assertTrue(engine.telemetryState.value.batteryBypass1PercentLockEnabled)
        }

        // Even in SAVING_MODE at 1% battery, selected 165Hz stays pinned
        engine.setRefreshRateTarget(com.example.model.RefreshRateTarget.HZ_165)
        engine.startPerformanceSession(
            profile = com.example.model.PerformanceProfile.SAVING_MODE,
            workloadFocus = com.example.model.WorkloadFocus.COMBINED_MAX
        )
        assertEquals(25, engine.telemetryState.value.lockedPowerPercent)
        assertEquals(165, engine.telemetryState.value.lockedRefreshRateHz)

        engine.stopPerformanceSession("Test cleanup")
        engine.testBatteryOverridePercent = null
    }

    @Test
    fun verifyGameAntiDropAndBackgroundHandoff() {
        engine.setRefreshRateTarget(com.example.model.RefreshRateTarget.HZ_120)
        engine.setEngineWorkloadMode(com.example.model.EngineWorkloadMode.GAME_ANTI_DROP)
        engine.startPerformanceSession(
            profile = com.example.model.PerformanceProfile.DIABLO_MODE,
            workloadFocus = com.example.model.WorkloadFocus.COMBINED_MAX
        )
        assertTrue(engine.telemetryState.value.antiFpsDropGuardEnabled)
        assertEquals(98, engine.telemetryState.value.gameCpuHeadroomPercent)
        assertEquals(120, engine.telemetryState.value.lockedRefreshRateHz)

        // Switching to BENCHMARK_STRESS while in foreground lowers headroom,
        // then backgrounding (launching a 120 FPS game) MUST free 99% headroom!
        engine.setEngineWorkloadMode(com.example.model.EngineWorkloadMode.BENCHMARK_STRESS)
        assertEquals(15, engine.telemetryState.value.gameCpuHeadroomPercent)

        engine.onAppForegroundStateChanged(inBackground = true)
        assertTrue(engine.telemetryState.value.backgroundZeroContentionActive)
        assertTrue(engine.telemetryState.value.antiFpsDropGuardEnabled)
        assertEquals(99, engine.telemetryState.value.gameCpuHeadroomPercent)
        assertEquals(120, engine.telemetryState.value.lockedRefreshRateHz)

        engine.onAppForegroundStateChanged(inBackground = false)
        engine.setEngineWorkloadMode(com.example.model.EngineWorkloadMode.GAME_ANTI_DROP)
        engine.stopPerformanceSession("Test cleanup")
    }

    @Test
    fun verifyRealTimeThermalMonitoringAndThrottlingAlert() {
        var alertTriggeredMessage: String? = null
        engine.onThermalAlertTriggered = { msg -> alertTriggeredMessage = msg }

        // 1. Cool state: no throttling
        engine.testThermalOverrideCelsius = 42.0f
        engine.refreshStaticAndDynamicTelemetry()
        val coolThermal = engine.telemetryState.value.thermalStatus
        assertFalse(coolThermal.isThrottling)
        assertEquals(null, coolThermal.alertHeadline)
        assertEquals(42.0f, coolThermal.cpuTempCelsius, 0.01f)
        assertEquals(40.0f, coolThermal.gpuTempCelsius, 0.01f)

        // 2. Hot throttling state: alert triggers, history tracks spike, dismissal works
        engine.testThermalOverrideCelsius = 76.0f
        engine.refreshStaticAndDynamicTelemetry()
        val hotState = engine.telemetryState.value
        assertTrue(hotState.thermalStatus.isThrottling)
        assertEquals("THERMAL THROTTLING ALERT", hotState.thermalStatus.alertHeadline)
        assertTrue(alertTriggeredMessage != null && alertTriggeredMessage!!.contains("76.0°C"))
        assertEquals(76.0f, hotState.cpuTempHistory.last(), 0.01f)

        engine.dismissThermalAlert()
        assertTrue(engine.telemetryState.value.thermalAlertDismissed)

        // 3. Cool down: alert clears and dismissal resets for future spikes
        engine.testThermalOverrideCelsius = 42.0f
        engine.refreshStaticAndDynamicTelemetry()
        assertFalse(engine.telemetryState.value.thermalStatus.isThrottling)
        assertFalse(engine.telemetryState.value.thermalAlertDismissed)

        engine.onThermalAlertTriggered = null
        engine.testThermalOverrideCelsius = null
    }

    @Test
    fun verifyFloatingFpsOverlayAndStabilityTelemetry() {
        assertTrue(engine.telemetryState.value.floatingFpsOverlayEnabled)

        engine.setRefreshRateTarget(com.example.model.RefreshRateTarget.HZ_120)
        engine.startPerformanceSession(
            profile = PerformanceProfile.DIABLO_MODE,
            workloadFocus = WorkloadFocus.COMBINED_MAX
        )
        val active = engine.telemetryState.value
        assertEquals(120, active.liveMeasuredFps)
        assertEquals(100, active.fpsStabilityPercent)
        assertEquals(16, active.fpsHistory.size)

        engine.setFloatingFpsOverlayEnabled(false)
        assertFalse(engine.telemetryState.value.floatingFpsOverlayEnabled)
        assertFalse(engine.telemetryState.value.floatingFpsOverlayVisible)

        engine.setFloatingFpsOverlayEnabled(true)
        assertTrue(engine.telemetryState.value.floatingFpsOverlayEnabled)

        engine.stopPerformanceSession("Test cleanup")
        assertFalse(engine.telemetryState.value.floatingFpsOverlayVisible)
    }

    @Test
    fun verifyCpuOnlyGpuOnlyAndCombinedWorkloadFocus() {
        // 1. CPU Multi-Core only: CPU runs at 100%, GPU stays idle at 0%
        engine.startPerformanceSession(
            profile = PerformanceProfile.DIABLO_MODE,
            workloadFocus = WorkloadFocus.CPU_FOCUS
        )
        var state = engine.telemetryState.value
        assertEquals(100, state.realMeasuredThreadDutyPercent)
        assertEquals(0, state.gpuLockedDutyPercent)
        assertEquals("IDLE (CPU FOCUS)", state.gpuStatus.requestState)

        // 2. Switch live to GPU Focused: GPU runs at 100%, CPU goes idle at 0%
        engine.setWorkloadFocus(WorkloadFocus.GPU_FOCUS)
        state = engine.telemetryState.value
        assertEquals(0, state.realMeasuredThreadDutyPercent)
        assertEquals(100, state.gpuLockedDutyPercent)
        assertEquals("IDLE (GPU FOCUS)", state.cpuStatus.requestState)

        // 3. Switch live to Combined Max: Both CPU and GPU run at 100%
        engine.setWorkloadFocus(WorkloadFocus.COMBINED_MAX)
        state = engine.telemetryState.value
        assertEquals(100, state.realMeasuredThreadDutyPercent)
        assertEquals(100, state.gpuLockedDutyPercent)

        engine.stopPerformanceSession("Test cleanup")
    }
}
