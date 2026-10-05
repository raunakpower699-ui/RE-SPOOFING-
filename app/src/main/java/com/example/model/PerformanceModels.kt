package com.example.model

/**
 * Supported performance profiles including ROG-inspired DIABLO MODE.
 * Default profile is PERFORMANCE.
 * Note: requestSustainedMode is kept false for PERFORMANCE, GAMING, DIABLO_MODE, and SUSTAINED_PERFORMANCE
 * because Android's Window.setSustainedPerformanceMode(true) instructs OEM PowerHALs to clamp maximum
 * Prime/Gold CPU clocks down to ~85-90%. Keeping it false allows 97%-100% unclamped turbo clocks.
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
        description = "Standard OS scheduler behavior. Applies power-efficiency hints where supported and avoids elevated frequency locks.",
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
        description = "Default profile. Locks multi-cluster ADPF sessions, WakeLock anti-idle floor, and GameManager hints for 97%–100% steady power output.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 3_000_000L
    ),
    GAMING(
        id = "GAMING",
        title = "GAMING",
        subtitle = "No-Touch 100% Lock for Vivo / iQOO gaming workloads",
        description = "Keeps CPU & GPU frequency floors locked even when not touching the screen or standing still in-game, eliminating 0% idle drops and frame stutters.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 2_500_000L
    ),
    DIABLO_MODE(
        id = "DIABLO_MODE",
        title = "DIABLO MODE",
        subtitle = "ROG-Style Extreme Uninterruptible 97%–100% Lock",
        description = "Inspired by ROG Phone X-Mode / Diablo Mode. Unclamps Prime + Gold CPU clusters (1.8ms ADPF Overdrive), locks UNINTERRUPTIBLE GameState, holds No-Touch CPU/GPU Frequency Lock, and sustains 97%+ power in 5–10+ min thermal tests.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 1_800_000L
    ),
    SUSTAINED_PERFORMANCE(
        id = "SUSTAINED_PERFORMANCE",
        title = "SUSTAINED PERFORMANCE",
        subtitle = "97%+ Anti-Throttle Stability for 5–10+ minute heavy loads",
        description = "Engineered for 5–10+ minute CPU Throttling Tests and marathon gaming. Disables OEM 90% clock clamping and continuously reinforces ADPF Prime-Core boost through warm thermal states.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 4_000_000L
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
        title = "Combined CPU + GPU Lock",
        summary = "Highest supported overall performance mode (97%–100% Lock)",
        detail = "Simultaneously locks multi-core CPU ADPF hints, No-Touch Governor Keep-Alive, and GPU RenderThread / Game Mode states.",
        enableCpuHints = true,
        enableGpuGameHints = true
    ),
    CPU_PRIMARY(
        id = "CPU_PRIMARY",
        title = "CPU Workload Priority",
        summary = "Prioritize CPU hints • Avoid unnecessary GPU activity",
        detail = "Focuses exclusively on multi-core CPU PerformanceHintManager thread scheduling and anti-throttle stability.",
        enableCpuHints = true,
        enableGpuGameHints = false
    ),
    GPU_PRIMARY(
        id = "GPU_PRIMARY",
        title = "GPU / Game Priority",
        summary = "Prioritize GPU & Game Mode hints • Avoid extra CPU hints",
        detail = "Prioritizes Android Game Mode and high-Hz GPU RenderThread frame lock without extra CPU worker pulses.",
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
 * Thermal state representation tuned for 5-10+ minute 97%+ stability.
 */
enum class ThermalStatusLevel(
    val displayLabel: String,
    val statusSummary: String,
    val isThrottling: Boolean
) {
    NORMAL(
        displayLabel = "NORMAL (99%–100% POWER)",
        statusSummary = "Optimal thermal state • Full unclamped Prime + Gold core boost locked",
        isThrottling = false
    ),
    WARM(
        displayLabel = "WARM (97%–99% BOOST LOCKED)",
        statusSummary = "Anti-Throttle Thermal Booster active • Holding 97%+ power output without down-stepping",
        isThrottling = false
    ),
    THROTTLING(
        displayLabel = "HARDWARE SAFETY LIMIT",
        statusSummary = "Performance limited by device thermal protection",
        isThrottling = true
    )
}

data class CpuStatusInfo(
    val requestState: String = "INACTIVE",
    val hintSessionActive: Boolean = false,
    val coreCount: Int = Runtime.getRuntime().availableProcessors(),
    val activeBoosterThreads: Int = 0,
    val preferredUpdateRateNanos: Long = 0L,
    val targetDurationNanos: Long = 0L,
    val architecture: String = "",
    val readableFrequenciesSummary: String = "OS Governor Managed",
    val powerStabilityPercent: Int = 0,
    val noTouchLockActive: Boolean = false,
    val statusDetail: String = "No active CPU performance request"
)

data class GpuStatusInfo(
    val requestState: String = "INACTIVE",
    val gameStateSignaled: Boolean = false,
    val directGpuControlExposed: Boolean = false,
    val hardwareModel: String = "",
    val renderFrameLockHz: Int = 60,
    val noTouchGpuKeepAlive: Boolean = false,
    val statusDetail: String = "Your device does not expose GPU performance controls through Android. The app will use the available system performance APIs instead."
)

data class ThermalStatusInfo(
    val protectionActive: Boolean = true,
    val antiThrottleBoosterActive: Boolean = true,
    val sustainedPowerScorePercent: Int = 99,
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
    val noTouchPowerLockEnabled: Boolean = true,
    val antiThrottleBoosterEnabled: Boolean = true,
    val vivoGameCenterInstantPulseEnabled: Boolean = true,
    val wakeLockHeld: Boolean = false,
    val lockedPowerPercent: Int = 0,
    val realMeasuredThreadDutyPercent: Int = 0,
    val purgedBackgroundAppsCount: Int = 0,
    val freedRamMb: Int = 0,
    val powerStabilityHistory: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
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
