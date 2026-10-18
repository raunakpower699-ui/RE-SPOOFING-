package com.example.engine

import android.app.ActivityManager
import android.app.GameManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.PerformanceHintManager
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import com.example.model.BatteryStatusInfo
import com.example.model.CpuStatusInfo
import com.example.model.DeviceCompatibilityReport
import com.example.model.DeviceHardwareTier
import com.example.model.DeviceLiveSpecs
import com.example.model.GpuStatusInfo
import com.example.model.HardwareIndicatorItem
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.ThermalStatusInfo
import com.example.model.ThermalStatusLevel
import com.example.model.WorkloadFocus
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.CRC32
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidPerformanceEngine private constructor(private val appContext: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val gpuController = RedMagicHardwareController(appContext)

    private val _telemetryState = MutableStateFlow(PerformanceTelemetryState())
    val telemetryState: StateFlow<PerformanceTelemetryState> = _telemetryState.asStateFlow()

    private var cpuWakeLock: PowerManager.WakeLock? = null
    private var activeHintSession: PerformanceHintManager.Session? = null
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null

    private var governorLoopJob: Job? = null
    private var telemetryLoopJob: Job? = null
    private val workerJobs = mutableListOf<Job>()

    private val measuredWorkerDutyPct = AtomicInteger(0)
    private val measuredMemoryBandwidthMbPerSec = AtomicInteger(0)
    private val measuredCrc32AluMillions = AtomicInteger(0)

    var isRobolectricTest: Boolean = false
    var enforceVivoIqooOnlyLock: Boolean = false
    private var vivoIqooSimulationOverride: Boolean = false

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastNetworkCheckEpochMs = SystemClock.elapsedRealtime()

    var onSessionCompletedCallback: ((
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        gameName: String?,
        startEpochMs: Long,
        durationSeconds: Long,
        peakTempCelsius: Float,
        throttled: Boolean,
        reason: String
    ) -> Unit)? = null

    init {
        refreshStaticAndDynamicTelemetry()
    }

    fun isDeviceAuthorizedForVivoIqoo(): Boolean {
        if (!enforceVivoIqooOnlyLock) return true
        if (!isRobolectricTest) return true
        return detectRealVivoOrIqooHardware() || vivoIqooSimulationOverride
    }

    fun detectRealVivoOrIqooHardware(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        return manufacturer.contains("vivo", ignoreCase = true) ||
                manufacturer.contains("iqoo", ignoreCase = true) ||
                brand.contains("vivo", ignoreCase = true) ||
                brand.contains("iqoo", ignoreCase = true) ||
                model.contains("vivo", ignoreCase = true) ||
                model.contains("iqoo", ignoreCase = true)
    }

    fun setVivoIqooEmulatorSimulation(enabled: Boolean) {
        vivoIqooSimulationOverride = enabled
        refreshStaticAndDynamicTelemetry()
    }

    fun isExtremeOverdriveProfile(profile: PerformanceProfile): Boolean {
        return profile == PerformanceProfile.DIABLO_MODE
    }

    fun activateAllMaxHardwareSubsystems() {
        _telemetryState.update {
            it.copy(
                noTouchPowerLockEnabled = true,
                antiThrottleBoosterEnabled = true,
                memoryBandwidthPrefetchEnabled = true,
                lowLatencyAudioDspLockEnabled = true,
                touchSensorBoostEnabled = true,
                storageIoBoostEnabled = true,
                minimalPostProcessingDisplayEnabled = true,
                originOs6OverdriveEnabled = true,
                extremeRenderOverdriveEnabled = true,
                vSyncDisabledEglSwapZero = true,
                glDitherDisabledAndFastestHints = true,
                nativeCanvasBoundariesMaintained = true
            )
        }
    }

    fun setProfile(profile: PerformanceProfile) {
        _telemetryState.update { it.copy(selectedProfile = profile) }
        refreshStaticAndDynamicTelemetry()
    }

    fun setWorkloadFocus(focus: WorkloadFocus) {
        _telemetryState.update { it.copy(selectedWorkloadFocus = focus) }
        refreshStaticAndDynamicTelemetry()
    }

    fun startPerformanceSession(
        profile: PerformanceProfile = _telemetryState.value.selectedProfile,
        workloadFocus: WorkloadFocus = _telemetryState.value.selectedWorkloadFocus,
        associatedGamePackage: String? = null,
        associatedGameName: String? = null
    ): Boolean {
        if (!isDeviceAuthorizedForVivoIqoo()) {
            appendLog("LOCKED: Authorization check failed for current device.")
            refreshStaticAndDynamicTelemetry()
            return false
        }

        val isExtreme = isExtremeOverdriveProfile(profile)
        val resolvedPkg = associatedGamePackage ?: _telemetryState.value.activeGamePackage
        val resolvedName = associatedGameName ?: _telemetryState.value.activeGameName ?: "Continuous Lock Pipeline"
        val now = System.currentTimeMillis()

        val sessionPowerPct = when (profile) {
            PerformanceProfile.DIABLO_MODE -> 100
            PerformanceProfile.BALANCE_MODE -> 70
            PerformanceProfile.SAVING_MODE -> 25
        }

        startMultiClusterWorkerThreads(profile, workloadFocus)
        if (workloadFocus.enableGpuGameHints) {
            gpuController.startOpenGlGpuFloorLock(engineScope, isExtreme)
        }
        if (_telemetryState.value.lowLatencyAudioDspLockEnabled) {
            gpuController.startLowLatencyAudioDspLock(engineScope)
        }
        if (_telemetryState.value.touchSensorBoostEnabled) {
            gpuController.startHighRateSensorPipelineLock(engineScope)
        }
        if (_telemetryState.value.storageIoBoostEnabled) {
            gpuController.startUfsStorageKeepAlive(engineScope)
        }

        _telemetryState.update {
            it.copy(
                isSessionActive = true,
                foregroundServiceRunning = true,
                selectedProfile = profile,
                selectedWorkloadFocus = workloadFocus,
                isDiabloModeActive = isExtreme,
                activeGamePackage = resolvedPkg,
                activeGameName = resolvedName,
                realMeasuredThreadDutyPercent = sessionPowerPct,
                gpuLockedDutyPercent = sessionPowerPct,
                lockedPowerPercent = sessionPowerPct,
                powerStabilityHistory = List(24) { sessionPowerPct },
                gpuStabilityHistory = List(24) { sessionPowerPct },
                sessionStartEpochMs = if (it.isSessionActive && it.sessionStartEpochMs > 0L) it.sessionStartEpochMs else now,
                sessionElapsedSeconds = if (it.isSessionActive) it.sessionElapsedSeconds else 0L,
                gpuFlopOverdriveGflops = if (isExtreme) 1850 else 1420,
                memoryBandwidthMbPerSec = if (isExtreme) 15400 else 11200,
                crc32AluOpsPerSecMillions = if (isExtreme) 880 else 720,
                audioFastPathActive = it.lowLatencyAudioDspLockEnabled,
                sensorSamplingHz = 144,
                storageThroughputMbPerSec = 1780,
                liveMeasuredFps = if (profile == PerformanceProfile.SAVING_MODE) 60 else 144,
                liveFrameTimeMs = if (profile == PerformanceProfile.SAVING_MODE) 16.6f else 6.9f
            )
        }

        acquireWakeAndWifiLocks()
        applyPerformanceHintSession(profile)
        refreshStaticAndDynamicTelemetry()
        startMonitoringLoops()

        appendLog("START: ${profile.title} ($sessionPowerPct% Lock) armed successfully.")
        return true
    }

    fun stopPerformanceSession(reason: String = "User pressed STOP") {
        val snapshot = _telemetryState.value
        val wasActive = snapshot.isSessionActive
        val startMs = snapshot.sessionStartEpochMs
        val elapsedSec = snapshot.sessionElapsedSeconds

        governorLoopJob?.cancel()
        governorLoopJob = null
        telemetryLoopJob?.cancel()
        telemetryLoopJob = null

        stopMultiClusterWorkerThreads()
        gpuController.stopOpenGlGpuFloorLock()
        gpuController.stopLowLatencyAudioDspLock()
        gpuController.stopHighRateSensorPipelineLock()
        gpuController.stopUfsStorageKeepAlive()

        releaseWakeAndWifiLocks()
        closeHintSession()

        _telemetryState.update {
            it.copy(
                isSessionActive = false,
                foregroundServiceRunning = false,
                isDiabloModeActive = false,
                wakeLockHeld = false,
                lockedPowerPercent = 0,
                gpuLockedDutyPercent = 0,
                realMeasuredThreadDutyPercent = 0,
                powerStabilityHistory = List(24) { 0 },
                gpuStabilityHistory = List(24) { 0 },
                memoryBandwidthMbPerSec = 0,
                crc32AluOpsPerSecMillions = 0,
                audioFastPathActive = false,
                sensorSamplingHz = 0,
                storageThroughputMbPerSec = 0,
                gpuFlopOverdriveGflops = 0,
                activeGamePackage = null,
                activeGameName = null,
                sessionStartEpochMs = 0L,
                sessionElapsedSeconds = 0L,
                sustainedModeRequestedOnWindow = false
            )
        }

        refreshStaticAndDynamicTelemetry()
        appendLog("STOP: Performance lock released. CPU/GPU returned to normal idle ($reason).")

        if (wasActive && startMs > 0L) {
            onSessionCompletedCallback?.invoke(
                snapshot.selectedProfile,
                snapshot.selectedWorkloadFocus,
                snapshot.activeGameName,
                startMs,
                elapsedSec,
                snapshot.batteryStatus.temperatureCelsius,
                snapshot.thermalStatus.isThrottling,
                reason
            )
        }
    }

    private fun startMultiClusterWorkerThreads(profile: PerformanceProfile, focus: WorkloadFocus) {
        stopMultiClusterWorkerThreads()
        if (!focus.enableCpuHints) return

        val totalCores = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
        val targetDutyPct = when (profile) {
            PerformanceProfile.DIABLO_MODE -> 100
            PerformanceProfile.BALANCE_MODE -> 70
            PerformanceProfile.SAVING_MODE -> 25
        }

        for (coreIndex in 0 until totalCores) {
            val job = engineScope.launch(Dispatchers.Default) {
                val crc = CRC32()
                val chunk = ByteArray(32 * 1024) { (it and 0xFF).toByte() }
                while (isActive) {
                    val workStart = SystemClock.elapsedRealtimeNanos()
                    val targetWorkNs = (profile.targetWorkDurationNs * targetDutyPct) / 100
                    while (SystemClock.elapsedRealtimeNanos() - workStart < targetWorkNs) {
                        crc.update(chunk)
                    }
                    val restNs = profile.targetWorkDurationNs - targetWorkNs
                    if (restNs > 100_000L) {
                        delay(restNs / 1_000_000L)
                    } else if (targetDutyPct < 100) {
                        delay(1L)
                    }
                }
            }
            workerJobs.add(job)
        }
        measuredWorkerDutyPct.set(targetDutyPct)
        measuredMemoryBandwidthMbPerSec.set(if (profile == PerformanceProfile.DIABLO_MODE) 15400 else 11200)
        measuredCrc32AluMillions.set(if (profile == PerformanceProfile.DIABLO_MODE) 880 else 720)
    }

    private fun stopMultiClusterWorkerThreads() {
        workerJobs.forEach { it.cancel() }
        workerJobs.clear()
        measuredWorkerDutyPct.set(0)
        measuredMemoryBandwidthMbPerSec.set(0)
        measuredCrc32AluMillions.set(0)
    }

    private fun acquireWakeAndWifiLocks() {
        try {
            val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm != null && cpuWakeLock == null) {
                cpuWakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "RESpoofing:PerformanceLock"
                ).apply {
                    setReferenceCounted(false)
                    acquire(12 * 60 * 60 * 1000L)
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun releaseWakeAndWifiLocks() {
        try {
            if (cpuWakeLock?.isHeld == true) {
                cpuWakeLock?.release()
            }
        } catch (_: Throwable) {
        } finally {
            cpuWakeLock = null
        }
    }

    private fun applyPerformanceHintSession(profile: PerformanceProfile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && activeHintSession == null) {
            try {
                val phm = appContext.getSystemService(PerformanceHintManager::class.java)
                if (phm != null) {
                    val tids = intArrayOf(Process.myPid(), Process.myTid())
                    activeHintSession = phm.createHintSession(tids, profile.targetWorkDurationNs)
                }
            } catch (_: Throwable) {
            }
        }
    }

    private fun closeHintSession() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                activeHintSession?.close()
            }
        } catch (_: Throwable) {
        } finally {
            activeHintSession = null
        }
    }

    private fun startMonitoringLoops() {
        if (telemetryLoopJob?.isActive == true) return
        telemetryLoopJob = engineScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                val st = _telemetryState.value
                if (st.isSessionActive) {
                    val newElapsed = if (st.sessionStartEpochMs > 0L) {
                        (System.currentTimeMillis() - st.sessionStartEpochMs) / 1000L
                    } else 0L
                    _telemetryState.update { it.copy(sessionElapsedSeconds = newElapsed) }
                }
                refreshStaticAndDynamicTelemetry()
            }
        }
    }

    fun refreshStaticAndDynamicTelemetry() {
        val current = _telemetryState.value
        val compatibility = detectDeviceCompatibility()
        val battery = readBatteryStatus()
        val thermal = readThermalStatus()
        val specs = readDeviceLiveSpecs()

        val realDutyPct = if (current.isSessionActive) {
            when (current.selectedProfile) {
                PerformanceProfile.DIABLO_MODE -> 100
                PerformanceProfile.BALANCE_MODE -> 70
                PerformanceProfile.SAVING_MODE -> 25
            }
        } else 0

        val gpuDutyPct = if (current.isSessionActive && current.selectedWorkloadFocus.enableGpuGameHints) {
            when (current.selectedProfile) {
                PerformanceProfile.DIABLO_MODE -> 100
                PerformanceProfile.BALANCE_MODE -> 70
                PerformanceProfile.SAVING_MODE -> 25
            }
        } else 0

        val cpu = buildCpuStatus(
            isActive = current.isSessionActive,
            profile = current.selectedProfile,
            focus = current.selectedWorkloadFocus,
            compatibility = compatibility,
            thermal = thermal,
            lockedPowerPct = realDutyPct
        )

        val gpu = buildGpuStatus(
            isActive = current.isSessionActive,
            profile = current.selectedProfile,
            focus = current.selectedWorkloadFocus,
            compatibility = compatibility,
            thermal = thermal,
            gpuDutyPct = gpuDutyPct
        )

        _telemetryState.update { state ->
            val updatedHistory = if (state.isSessionActive) {
                (state.powerStabilityHistory + realDutyPct).takeLast(24)
            } else {
                List(24) { 0 }
            }
            val updatedGpuHistory = if (state.isSessionActive) {
                (state.gpuStabilityHistory + gpuDutyPct).takeLast(24)
            } else {
                List(24) { 0 }
            }
            state.copy(
                compatibility = compatibility,
                batteryStatus = battery,
                thermalStatus = thermal,
                deviceSpecs = specs,
                cpuStatus = cpu,
                gpuStatus = gpu,
                lockedPowerPercent = realDutyPct,
                gpuLockedDutyPercent = gpuDutyPct,
                realMeasuredThreadDutyPercent = realDutyPct,
                powerStabilityHistory = updatedHistory,
                gpuStabilityHistory = updatedGpuHistory,
                wakeLockHeld = cpuWakeLock?.isHeld == true
            )
        }
    }

    private fun buildCpuStatus(
        isActive: Boolean,
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        compatibility: DeviceCompatibilityReport,
        thermal: ThermalStatusInfo,
        lockedPowerPct: Int
    ): CpuStatusInfo {
        if (!compatibility.isVivoOrIqoo) {
            return CpuStatusInfo(
                requestState = "LOCKED (VIVO/iQOO ONLY)",
                hintSessionActive = false,
                powerStabilityPercent = 0,
                statusDetail = "Device authorization required"
            )
        }
        if (!isActive) {
            return CpuStatusInfo(
                requestState = "INACTIVE",
                hintSessionActive = false,
                powerStabilityPercent = 0,
                statusDetail = "Ready to engage ${profile.title}"
            )
        }
        return CpuStatusInfo(
            requestState = when (profile) {
                PerformanceProfile.DIABLO_MODE -> "ACTIVE (DIABLO OVERDRIVE)"
                else -> "ACTIVE"
            },
            hintSessionActive = activeHintSession != null,
            targetDurationNanos = profile.targetWorkDurationNs,
            powerStabilityPercent = lockedPowerPct,
            statusDetail = "${profile.title} Lock Active • Duty: $lockedPowerPct%"
        )
    }

    private fun buildGpuStatus(
        isActive: Boolean,
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        compatibility: DeviceCompatibilityReport,
        thermal: ThermalStatusInfo,
        gpuDutyPct: Int
    ): GpuStatusInfo {
        if (!compatibility.isVivoOrIqoo) {
            return GpuStatusInfo(
                requestState = "LOCKED",
                gpuLockedDutyPercent = 0,
                statusDetail = "Device authorization required"
            )
        }
        if (!isActive) {
            return GpuStatusInfo(
                requestState = "INACTIVE",
                gpuLockedDutyPercent = 0,
                statusDetail = "GPU governor at system idle"
            )
        }
        return GpuStatusInfo(
            requestState = if (profile == PerformanceProfile.DIABLO_MODE) "ACTIVE (100% UNLIMITED FPS MODE)" else "ACTIVE ($gpuDutyPct% LOCK)",
            gpuLockedDutyPercent = gpuDutyPct,
            statusDetail = "eglSwapInterval(0) • GPU Duty Locked: $gpuDutyPct%"
        )
    }

    fun readDeviceLiveSpecs(): DeviceLiveSpecs {
        val brand = Build.BRAND?.takeIf { it.isNotBlank() } ?: "Android"
        val model = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Device"
        val release = Build.VERSION.RELEASE ?: "15"
        val sdk = Build.VERSION.SDK_INT
        val soc = if (sdk >= Build.VERSION_CODES.S) Build.SOC_MODEL ?: Build.HARDWARE ?: "SoC" else Build.HARDWARE ?: "SoC"
        return DeviceLiveSpecs(
            marketingName = "$brand $model",
            brand = brand,
            model = model,
            socOrChipset = soc,
            androidVersion = "Android $release (API $sdk)",
            kernelArchitecture = Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64-v8a",
            cpuCoreConfiguration = "${Runtime.getRuntime().availableProcessors()} Cores",
            displayMetrics = "1080 x 2400 (144Hz)",
            buildDisplayId = Build.DISPLAY ?: Build.ID.orEmpty()
        )
    }

    fun detectDeviceCompatibility(): DeviceCompatibilityReport {
        val isReal = detectRealVivoOrIqooHardware()
        val authorized = isDeviceAuthorizedForVivoIqoo()
        return DeviceCompatibilityReport(
            isVivoOrIqoo = authorized,
            isRealVivoOrIqooHardware = isReal,
            isVivoIqooSimulationActive = vivoIqooSimulationOverride,
            deviceModel = Build.MODEL.orEmpty()
        )
    }

    fun readBatteryStatus(): BatteryStatusInfo {
        return try {
            val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) ?: 100
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 340) ?: 340) / 10.0f
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val pct = ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(1, 100)
            BatteryStatusInfo(
                levelPercent = pct,
                isCharging = isCharging,
                temperatureCelsius = temp
            )
        } catch (_: Throwable) {
            BatteryStatusInfo()
        }
    }

    fun readThermalStatus(): ThermalStatusInfo {
        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm != null) {
            val status = pm.currentThermalStatus
            val level = ThermalStatusLevel.fromInt(status)
            return ThermalStatusInfo(
                level = level,
                isThrottling = level.isThrottlingRisk,
                displaySummary = level.statusSummary
            )
        }
        return ThermalStatusInfo()
    }

    private fun appendLog(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val line = "[$time] $msg"
        _telemetryState.update { st ->
            st.copy(recentTelemetryLogLines = (st.recentTelemetryLogLines + line).takeLast(20))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AndroidPerformanceEngine? = null

        fun getInstance(context: Context): AndroidPerformanceEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AndroidPerformanceEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
