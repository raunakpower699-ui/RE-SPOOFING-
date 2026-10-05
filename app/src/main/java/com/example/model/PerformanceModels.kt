package com.example.model

/**
 * Supported performance profiles including ROG-inspired DIABLO MODE.
 * Default profile is PERFORMANCE.
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
    BALANCED(
        id = "BALANCED",
        title = "BALANCED",
        subtitle = "Normal Vivo / iQOO Android behavior",
        description = "Standard OS scheduler behavior. Applies power-efficiency hints where supported and avoids elevated frequency requests.",
        requestCpuHints = true,
        requestGpuOrGameHints = false,
        requestSustainedMode = false,
        preferPowerEfficiency = true,
        targetWorkDurationNs = 16_666_666L
    ),
    PERFORMANCE(
        id = "PERFORMANCE",
        title = "PERFORMANCE",
        subtitle = "Requests the highest supported Vivo / iQOO performance level",
        description = "Default profile. Requests maximum available performance within official Android & Vivo/iQOO limits via PerformanceHintManager and GameManager.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 8_333_333L
    ),
    GAMING(
        id = "GAMING",
        title = "GAMING",
        subtitle = "Optimized for Vivo / iQOO interactive gaming workloads",
        description = "Prioritizes low-latency frame pacing, Android Game Mode APIs (GAME_MODE_PERFORMANCE / GameState), and interactive thread hints.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 8_333_333L
    ),
    DIABLO_MODE(
        id = "DIABLO_MODE",
        title = "DIABLO MODE",
        subtitle = "ROG-Style Extreme Uninterruptible Overdrive",
        description = "Inspired by ROG Phone X-Mode / Diablo Mode. Requests ultra-tight 6.0ms (165Hz-class) CPU/render thread targets, signals UNINTERRUPTIBLE competitive GameState, activates Sustained Window Mode, and requests peak display refresh rate while respecting OS hardware safety limits.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = true,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 6_000_000L
    ),
    SUSTAINED_PERFORMANCE(
        id = "SUSTAINED_PERFORMANCE",
        title = "SUSTAINED PERFORMANCE",
        subtitle = "Consistent output for longer Vivo / iQOO workloads",
        description = "Activates Android Window Sustained Performance Mode and steady CPU work targets for extended sessions while strictly respecting thermal limits.",
        requestCpuHints = true,
        requestGpuOrGameHints = false,
        requestSustainedMode = true,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 11_111_111L
    );

    companion object {
        fun fromId(id: String?): PerformanceProfile {
            return entries.find { it.id == id } ?: PERFORMANCE
        }
    }
}

/**
 * Intelligent CPU/GPU workload optimization focus (Section 4).
 */
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
        title = "Combined CPU + GPU",
        summary = "Highest supported overall performance mode",
        detail = "For heavy combined workloads: requests both CPU PerformanceHintSession and Game Mode / GPU rendering hints where exposed.",
        enableCpuHints = true,
        enableGpuGameHints = true
    ),
    CPU_PRIMARY(
        id = "CPU_PRIMARY",
        title = "CPU Workload Priority",
        summary = "Prioritize CPU hints • Avoid unnecessary GPU activity",
        detail = "Focuses exclusively on CPU PerformanceHintManager thread scheduling hints without forcing GPU or Game Mode rendering states.",
        enableCpuHints = true,
        enableGpuGameHints = false
    ),
    GPU_PRIMARY(
        id = "GPU_PRIMARY",
        title = "GPU / Game Priority",
        summary = "Prioritize GPU & Game Mode hints • Avoid extra CPU hints",
        detail = "Prioritizes Android Game Mode / rendering hints where exposed by Android/OEM APIs without keeping CPU hint sessions active.",
        enableCpuHints = false,
        enableGpuGameHints = true
    );

    companion object {
        fun fromId(id: String?): WorkloadFocus {
            return entries.find { it.id == id } ?: COMBINED_MAX
        }
    }
}

/**
 * Honest thermal state representation (Section 4 & 5).
 */
enum class ThermalStatusLevel(
    val displayLabel: String,
    val statusSummary: String,
    val isThrottling: Boolean
) {
    NORMAL(
        displayLabel = "NORMAL",
        statusSummary = "Optimal thermal state • Full supported performance available",
        isThrottling = false
    ),
    WARM(
        displayLabel = "WARM",
        statusSummary = "Elevated temperature • Thermal Protection actively monitoring",
        isThrottling = false
    ),
    THROTTLING(
        displayLabel = "THROTTLING",
        statusSummary = "Performance limited by device thermal protection",
        isThrottling = true
    )
}

data class CpuStatusInfo(
    val requestState: String = "INACTIVE",
    val hintSessionActive: Boolean = false,
    val coreCount: Int = Runtime.getRuntime().availableProcessors(),
    val preferredUpdateRateNanos: Long = 0L,
    val targetDurationNanos: Long = 0L,
    val architecture: String = "",
    val readableFrequenciesSummary: String = "OS Governor Managed",
    val statusDetail: String = "No active CPU performance request"
)

data class GpuStatusInfo(
    val requestState: String = "INACTIVE",
    val gameStateSignaled: Boolean = false,
    val directGpuControlExposed: Boolean = false,
    val hardwareModel: String = "",
    val statusDetail: String = "Your device does not expose GPU performance controls through Android. The app will use the available system performance APIs instead."
)

data class ThermalStatusInfo(
    val protectionActive: Boolean = true,
    val level: ThermalStatusLevel = ThermalStatusLevel.NORMAL,
    val rawAndroidThermalCode: Int = 0,
    val rawAndroidThermalName: String = "THERMAL_STATUS_NONE",
    val thermalHeadroom: Float? = null,
    val batteryTempCelsius: Float = 0f,
    val isThrottling: Boolean = false,
    val warningBannerText: String? = null
)

data class BatteryStatusInfo(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val powerSourceLabel: String = "Discharging",
    val temperatureCelsius: Float = 28.0f,
    val voltageMv: Int = 4000,
    val isLowBattery: Boolean = false,
    val isSystemBatterySaverOn: Boolean = false
)

/**
 * Live device hardware & software specifications shown to every user who downloads the app.
 */
data class DeviceLiveSpecs(
    val phoneDisplayName: String = "Android Smartphone",
    val manufacturer: String = "Unknown",
    val brand: String = "Unknown",
    val model: String = "Android",
    val deviceCodename: String = "generic",
    val boardName: String = "board",
    val androidVersionLabel: String = "Android",
    val securityPatch: String = "Standard",
    val buildId: String = "",
    val socProcessor: String = "ARM64 Processor",
    val cpuCores: Int = 8,
    val cpuAbi: String = "arm64-v8a",
    val totalRamGb: String = "8.0 GB",
    val availableRamGb: String = "4.0 GB",
    val ramUsedPercent: Int = 50,
    val totalStorageGb: String = "64.0 GB",
    val freeStorageGb: String = "32.0 GB",
    val screenResolution: String = "1080 x 2400",
    val displayRefreshRateHz: Int = 60
)

data class DeviceCompatibilityReport(
    val androidRelease: String = "",
    val sdkInt: Int = 0,
    val manufacturer: String = "",
    val brand: String = "",
    val deviceModel: String = "",
    val socOrHardware: String = "",
    val isVivoOrIqoo: Boolean = false,
    val isRealVivoOrIqooHardware: Boolean = false,
    val isVivoIqooSimulationActive: Boolean = false,
    val vivoOsInfo: String = "Vivo / iQOO Verification Pending",
    val cpuPerformanceHintSupported: Boolean = false,
    val cpuPowerEfficiencySupported: Boolean = false,
    val gpuPerformanceHintExposed: Boolean = false,
    val gameModeSupported: Boolean = false,
    val currentGameModeLabel: String = "UNSUPPORTED",
    val sustainedPerformanceSupported: Boolean = false,
    val thermalMonitoringSupported: Boolean = false,
    val thermalHeadroomSupported: Boolean = false,
    val oemPerformanceControlStatus: String = "OEM performance control unavailable to this application."
)

data class ActiveOptimizationIndicator(
    val id: String,
    val title: String,
    val stateLabel: String,
    val description: String,
    val isActive: Boolean,
    val isFallbackOrLimited: Boolean = false
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

data class PerformanceTelemetryState(
    val isSessionActive: Boolean = false,
    val foregroundServiceRunning: Boolean = false,
    val selectedProfile: PerformanceProfile = PerformanceProfile.PERFORMANCE,
    val selectedWorkloadFocus: WorkloadFocus = WorkloadFocus.COMBINED_MAX,
    val isDiabloModeActive: Boolean = false,
    val activeGamePackage: String? = null,
    val activeGameName: String? = null,
    val sessionStartEpochMs: Long = 0L,
    val sessionElapsedSeconds: Long = 0L,
    val cpuStatus: CpuStatusInfo = CpuStatusInfo(),
    val gpuStatus: GpuStatusInfo = GpuStatusInfo(),
    val thermalStatus: ThermalStatusInfo = ThermalStatusInfo(),
    val batteryStatus: BatteryStatusInfo = BatteryStatusInfo(),
    val deviceSpecs: DeviceLiveSpecs = DeviceLiveSpecs(),
    val compatibility: DeviceCompatibilityReport = DeviceCompatibilityReport(),
    val activeIndicators: List<ActiveOptimizationIndicator> = emptyList(),
    val eventLogs: List<String> = emptyList(),
    val sustainedModeRequestedOnWindow: Boolean = false
)
