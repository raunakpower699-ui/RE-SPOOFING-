package com.example.model

/**
 * Supported performance profiles:
 * - DIABLO_MODE: 100% Unclamped CPU & GPU Lock, Anti-Throttle, Max Performance.
 * - BALANCE_MODE: 70% Target Floor, Optimal Gaming & Thermals.
 * - SAVING_MODE: 25% Throttled Floor, Battery Saver & Rapid Cool-Down.
 */
enum class PerformanceProfile(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val requestCpuHints: Boolean,
    val requestGpuOrGameHints: Boolean,
    val requestSustainedMode: Boolean,
    val preferPowerEfficiency: Boolean,
    val targetWorkDurationNs: Long
) {
    DIABLO_MODE(
        id = "DIABLO_MODE",
        title = "Diablo Mode",
        subtitle = "Extreme 100% Lock • Max Performance",
        description = "Extreme unclamped 100% CPU and 100% GPU continuous lock. Zero throttling, full compute core saturation (Prime, Gold, Silver), continuous OpenGL ES / Vulkan 3D pipeline, eglSwapInterval(0), and sustained hardware power delivery.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 1_000_000L
    ),
    BALANCE_MODE(
        id = "BALANCE_MODE",
        title = "Balance Mode",
        subtitle = "Optimal Gaming • Adaptive Clocks (70% Target)",
        description = "Balanced performance profile for high-end gaming and heavy multitasking. Maintains a steady 70% power floor with smooth frame pacing, eliminating frame drops while keeping thermals and battery balanced.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 8_333_333L
    ),
    SAVING_MODE(
        id = "SAVING_MODE",
        title = "Saving Mode",
        subtitle = "Battery Saver • Thermal Cool-Down (25% Target)",
        description = "Energy saving and thermal cool-down profile. Throttles CPU and GPU to ~25%, cools down hardware rapidly after heavy runs, caps screen refresh to 60Hz, and extends battery life.",
        requestCpuHints = false,
        requestGpuOrGameHints = false,
        requestSustainedMode = false,
        preferPowerEfficiency = true,
        targetWorkDurationNs = 16_666_666L
    );

    companion object {
        val ORIGINOS_6_OVERDRIVE: PerformanceProfile get() = DIABLO_MODE
        val GAMING: PerformanceProfile get() = DIABLO_MODE
        val PERFORMANCE: PerformanceProfile get() = DIABLO_MODE
        val SUSTAINED_PERFORMANCE: PerformanceProfile get() = DIABLO_MODE
        val BALANCED: PerformanceProfile get() = BALANCE_MODE

        fun fromId(id: String?): PerformanceProfile {
            return when (id?.uppercase()) {
                "DIABLO_MODE", "ORIGINOS_6_OVERDRIVE", "GAMING", "PERFORMANCE", "SUSTAINED_PERFORMANCE" -> DIABLO_MODE
                "SAVING_MODE", "SAVING" -> SAVING_MODE
                "BALANCE_MODE", "BALANCED" -> BALANCE_MODE
                else -> DIABLO_MODE
            }
        }
    }
}

enum class WorkloadFocus(
    val id: String,
    val title: String,
    val summary: String,
    val detail: String,
    val enableCpuHints: Boolean,
    val enableGpuGameHints: Boolean
) {
    COMBINED_MAX(
        id = "COMBINED_MAX",
        title = "Combined CPU + GPU Lock",
        summary = "Highest supported overall performance mode (100% Lock)",
        detail = "Simultaneously locks multi-core CPU FPU/CRC32C + ADPF hints, LPDDR memory bus, Audio DSP, Sensors, UFS I/O, and OpenGL ES 3.0/2.0 at 100%.",
        enableCpuHints = true,
        enableGpuGameHints = true
    ),
    CPU_PRIMARY(
        id = "CPU_PRIMARY",
        title = "CPU Workload Priority",
        summary = "Prioritize CPU hints • Avoid unnecessary GPU activity",
        detail = "Focuses exclusively on multi-core CPU PerformanceHintManager thread scheduling, FPU/CRC32C ALU, and anti-throttle stability.",
        enableCpuHints = true,
        enableGpuGameHints = false
    ),
    GPU_PRIMARY(
        id = "GPU_PRIMARY",
        title = "GPU / Game Priority",
        summary = "Prioritize GPU & Game Mode hints • Avoid extra CPU hints",
        detail = "Prioritizes Android Game Mode, 3D OpenGL ES/Vulkan pipeline, and eglSwapInterval = 0 unlocked frame pacing.",
        enableCpuHints = false,
        enableGpuGameHints = true
    );

    companion object {
        fun fromId(id: String?): WorkloadFocus {
            return entries.find { it.id == id } ?: COMBINED_MAX
        }
    }
}

enum class ThermalStatusLevel(
    val displayLabel: String,
    val statusSummary: String,
    val isSevereOrCritical: Boolean,
    val isThrottlingRisk: Boolean
) {
    NONE("NOMINAL (STABLE)", "Hardware operating within cool thermal parameters. Full boost available.", false, false),
    LIGHT("ELEVATED (NORMAL)", "Moderate surface temperature rise. Governor floor holds 100%.", false, false),
    MODERATE("WARM (BOOSTED)", "Active heat dissipation ongoing. Anti-throttle booster holds clocks high.", false, false),
    SEVERE("HOT (MITIGATED)", "Thermal floor protection engaging. Anti-throttle booster active.", true, true),
    CRITICAL("CRITICAL (PROTECTED)", "Maximum sustained emergency cooling.", true, true),
    EMERGENCY("EMERGENCY SHUTDOWN", "Device thermal threshold reached. Auto-throttle protection active.", true, true),
    SHUTDOWN("HARDWARE SHUTDOWN", "Shutdown imminent.", true, true);

    companion object {
        fun fromInt(value: Int): ThermalStatusLevel {
            return when (value) {
                0 -> NONE
                1 -> LIGHT
                2 -> MODERATE
                3 -> SEVERE
                4 -> CRITICAL
                5 -> EMERGENCY
                6 -> SHUTDOWN
                else -> NONE
            }
        }
    }
}

data class BatteryStatusInfo(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 34.0f,
    val voltageMv: Int = 4200,
    val health: String = "Good",
    val isLowBatteryWarning: Boolean = false
)

data class CpuStatusInfo(
    val requestState: String = "INACTIVE",
    val hintSessionActive: Boolean = false,
    val coreCount: Int = 8,
    val activeBoosterThreads: Int = 0,
    val preferredUpdateRateNanos: Long = 16_666_666L,
    val targetDurationNanos: Long = 1_000_000L,
    val architecture: String = "arm64-v8a",
    val readableFrequenciesSummary: String = "Standard CPU Clocks",
    val powerStabilityPercent: Int = 0,
    val noTouchLockActive: Boolean = false,
    val statusDetail: String = "System default CPU scheduler active"
)

data class GpuStatusInfo(
    val requestState: String = "INACTIVE",
    val gameStateSignaled: Boolean = false,
    val directGpuControlExposed: Boolean = false,
    val hardwareModel: String = "Adreno / Mali GPU",
    val renderFrameLockHz: Int = 144,
    val gpuLockedDutyPercent: Int = 0,
    val noTouchGpuKeepAlive: Boolean = false,
    val statusDetail: String = "System default GPU governor active"
)

data class ThermalStatusInfo(
    val level: ThermalStatusLevel = ThermalStatusLevel.NONE,
    val headroomRatio: Float? = null,
    val isThrottling: Boolean = false,
    val displaySummary: String = "Thermals nominal • Full 100% boost available"
)

data class DeviceLiveSpecs(
    val marketingName: String = "Android Device",
    val brand: String = "Android",
    val model: String = "Model",
    val socOrChipset: String = "Snapdragon / Dimensity",
    val androidVersion: String = "Android 15 (API 35)",
    val kernelArchitecture: String = "arm64-v8a",
    val cpuCoreConfiguration: String = "8 Cores (Prime + Gold + Silver)",
    val totalRamString: String = "12.0 GB",
    val availableRamString: String = "7.8 GB",
    val totalInternalStorage: String = "256 GB",
    val availableInternalStorage: String = "180 GB",
    val displayMetrics: String = "1080 x 2400 (144Hz HDR)",
    val buildDisplayId: String = "Build"
) {
    val phoneDisplayName: String get() = "$brand $model ($marketingName)"
    val androidVersionLabel: String get() = androidVersion
    val cpuCores: Int get() = 8
}

enum class DeviceHardwareTier(
    val code: String,
    val shortLabel: String,
    val description: String,
    val peakTargetGflops: Int
) {
    ULTRA_FLAGSHIP("ULTRA", "Ultra Tier", "Flagship Processor with maximum compute capability", 2400),
    PREMIUM_HIGH("PREMIUM", "Premium Tier", "High-End Processor with robust compute capability", 1800),
    MID_BALANCED("MID", "Balanced Tier", "Balanced Processor for solid gaming and multitasking", 1400);

    companion object {
        fun detect(cores: Int, totalRamGb: Double, soc: String, model: String): DeviceHardwareTier {
            val upper = "$soc $model".uppercase()
            return when {
                upper.contains("8 GEN") || upper.contains("9300") || upper.contains("9400") || totalRamGb >= 11.5 -> ULTRA_FLAGSHIP
                upper.contains("7+") || upper.contains("870") || upper.contains("888") || upper.contains("8200") || totalRamGb >= 7.5 -> PREMIUM_HIGH
                else -> MID_BALANCED
            }
        }
    }
}

data class DeviceCompatibilityReport(
    val androidRelease: String = "15",
    val sdkInt: Int = 35,
    val manufacturer: String = "Android",
    val brand: String = "Android",
    val deviceModel: String = "Device",
    val socOrHardware: String = "Snapdragon",
    val isVivoOrIqoo: Boolean = true,
    val isRealVivoOrIqooHardware: Boolean = true,
    val isVivoIqooSimulationActive: Boolean = false,
    val vivoOsInfo: String = "Performance Engine Active",
    val cpuPerformanceHintSupported: Boolean = true,
    val cpuPowerEfficiencySupported: Boolean = false,
    val gpuPerformanceHintExposed: Boolean = false,
    val gameModeSupported: Boolean = true,
    val currentGameModeLabel: String = "GAME_MODE_PERFORMANCE",
    val sustainedPerformanceSupported: Boolean = true,
    val thermalMonitoringSupported: Boolean = true,
    val thermalHeadroomSupported: Boolean = true,
    val oemPerformanceControlStatus: String = "PERFORMANCE_PIPELINE_ACTIVE",
    val hardwareTier: DeviceHardwareTier = DeviceHardwareTier.ULTRA_FLAGSHIP
)

data class HardwareIndicatorItem(
    val key: String,
    val title: String,
    val statusText: String,
    val isEngaged: Boolean,
    val explanation: String
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String
)

data class PerformanceTelemetryState(
    val isSessionActive: Boolean = false,
    val foregroundServiceRunning: Boolean = false,
    val selectedProfile: PerformanceProfile = PerformanceProfile.DIABLO_MODE,
    val selectedWorkloadFocus: WorkloadFocus = WorkloadFocus.COMBINED_MAX,
    val isDiabloModeActive: Boolean = false,
    val wakeLockHeld: Boolean = false,
    val lockedPowerPercent: Int = 0,
    val gpuLockedDutyPercent: Int = 0,
    val realMeasuredThreadDutyPercent: Int = 0,
    val memoryBandwidthMbPerSec: Int = 0,
    val crc32AluOpsPerSecMillions: Int = 0,
    val audioFastPathActive: Boolean = false,
    val sensorSamplingHz: Int = 0,
    val storageThroughputMbPerSec: Int = 0,
    val liveMeasuredFps: Int = 144,
    val liveFrameTimeMs: Float = 6.9f,
    val targetFrameRateFps: Int = 144,
    val powerStabilityHistory: List<Int> = List(24) { 0 },
    val gpuStabilityHistory: List<Int> = List(24) { 0 },
    val activeGamePackage: String? = null,
    val activeGameName: String? = null,
    val sessionStartEpochMs: Long = 0L,
    val sessionElapsedSeconds: Long = 0L,
    val sustainedModeRequestedOnWindow: Boolean = false,
    val noTouchPowerLockEnabled: Boolean = true,
    val antiThrottleBoosterEnabled: Boolean = true,
    val memoryBandwidthPrefetchEnabled: Boolean = true,
    val lowLatencyAudioDspLockEnabled: Boolean = true,
    val touchSensorBoostEnabled: Boolean = true,
    val storageIoBoostEnabled: Boolean = true,
    val minimalPostProcessingDisplayEnabled: Boolean = true,
    val originOs6OverdriveEnabled: Boolean = true,
    val extremeRenderOverdriveEnabled: Boolean = true,
    val vSyncDisabledEglSwapZero: Boolean = true,
    val glDitherDisabledAndFastestHints: Boolean = true,
    val nativeCanvasBoundariesMaintained: Boolean = true,
    val renderScaleSpoofEnabled: Boolean = false,
    val renderScaleFactor: Float = 1.00f,
    val internalShaderResolutionLabel: String = "1080 x 2400 (Zero Surface Downsampling)",
    val globalDisplayDpiLabel: String = "1.0x NATIVE CANVAS",
    val gpuFlopOverdriveGflops: Int = 0,
    val vivoPemThermalDaemonSuppressed: Boolean = true,
    val volumeShaderRayStepsPerFrame: Int = 64,
    val originOsOutputStatus: String = "VSYNC_DISABLED: eglSwapInterval(0) | GL_FASTEST_THROUGHPUT | NATIVE_1.0X_CANVAS",
    val secondaryDirectiveStatus: String = "UNLIMITED_FPS_BENCHMARK_ACTIVE | GL_DITHER: OFF | THERMAL_BYPASS: ENGAGED",
    val targetPipelineProcess: String = "com.android.opengl / OpenGL ES 3.0",
    val sensorHardwareName: String = "High Rate IMU",
    val liveDownloadBandwidthMbps: Float = 0f,
    val liveUploadBandwidthMbps: Float = 0f,
    val recentTelemetryLogLines: List<String> = emptyList(),
    val batteryStatus: BatteryStatusInfo = BatteryStatusInfo(),
    val thermalStatus: ThermalStatusInfo = ThermalStatusInfo(),
    val deviceSpecs: DeviceLiveSpecs = DeviceLiveSpecs(),
    val cpuStatus: CpuStatusInfo = CpuStatusInfo(),
    val gpuStatus: GpuStatusInfo = GpuStatusInfo(),
    val compatibility: DeviceCompatibilityReport = DeviceCompatibilityReport(),
    val activeIndicators: List<HardwareIndicatorItem> = emptyList()
)
