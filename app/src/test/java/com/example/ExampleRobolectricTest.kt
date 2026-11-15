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
}
