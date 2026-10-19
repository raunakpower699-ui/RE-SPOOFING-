package com.example.model

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
        title = "Battery Saving Mode",
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
                "SAVING_MODE", "SAVING", "BATTERY_SAVING_MODE", "BATTERY_SAVER" -> SAVING_MODE
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
        title = "Combined Max",
        summary = "CPU & GPU full compute priority",
        detail = "Pins both CPU clusters and GPU rendering pipeline to maximize overall benchmark scores and competitive gaming frame pacing.",
        enableCpuHints = true,
        enableGpuGameHints = true
    ),
    GPU_FOCUS(
        id = "GPU_FOCUS",
        title = "GPU Focused",
        summary = "Sustained frame pacing & raw shader throughput",
        detail = "Prioritizes GPU clock locking and V-Sync bypass (eglSwapInterval 0) for demanding 3D games and emulators.",
        enableCpuHints = false,
        enableGpuGameHints = true
    ),
    CPU_FOCUS(
        id = "CPU_FOCUS",
        title = "CPU Multi-Core",
        summary = "Prime & Gold core frequency saturation",
        detail = "Forces high frequency on Prime and Gold cores, bypassing low-power Silver governor throttling for physics and compute.",
        enableCpuHints = true,
        enableGpuGameHints = false
    );

    companion object {
        fun fromId(id: String?): WorkloadFocus {
            return when (id?.uppercase()) {
                "GPU_FOCUS" -> GPU_FOCUS
                "CPU_FOCUS" -> CPU_FOCUS
                else -> COMBINED_MAX
            }
        }
    }
}

enum class DeviceHardwareTier(val displayName: String, val badgeColorHex: String) {
    FLAGSHIP_TIER("Flagship Tier (Extreme Ready)", "#FF2A4D"),
    HIGH_PERF_TIER("High Performance Tier", "#00E5FF"),
    STANDARD_TIER("Standard Tier", "#00E676");
}

data class DeviceLiveSpecs(
    val marketingName: String = "Android Device",
    val brand: String = "Android",
    val model: String = "Device",
    val socOrChipset: String = "High Performance SoC",
    val androidVersion: String = "Android 15",
    val kernelArchitecture: String = "arm64-v8a",
    val cpuCoreConfiguration: String = "8 Cores (1 Prime + 3 Gold + 4 Silver)",
    val displayMetrics: String = "1080 x 2400 (144Hz)",
    val buildDisplayId: String = ""
) {
    val phoneDisplayName: String
        get() {
            val raw = if (brand.isNotBlank() && model.isNotBlank()) "$brand $model" else "Gaming Device"
            if (raw.contains("sdk", ignoreCase = true) || raw.contains("gphone", ignoreCase = true) || raw.contains("goldfish", ignoreCase = true) || raw.contains("google", ignoreCase = true) || raw.contains("emulator", ignoreCase = true)) {
                return "Extreme Gaming Core • Performance Tuned"
            }
            return raw.replaceFirstChar { it.uppercase() }
        }

    val androidVersionLabel: String
        get() = androidVersion
}

enum class ThermalStatusLevel(val intValue: Int, val isThrottlingRisk: Boolean, val statusSummary: String) {
    NONE(0, false, "Nominal / Cool"),
    LIGHT(1, false, "Light Thermal Rise"),
    MODERATE(2, false, "Moderate Heat"),
    SEVERE(3, true, "Severe Warning (Throttling Protected)"),
    CRITICAL(4, true, "Critical (Bypass Safeguard Active)"),
    EMERGENCY(5, true, "Emergency Cutoff"),
    SHUTDOWN(6, true, "Hardware Shutdown Trigger");

    companion object {
        fun fromInt(value: Int): ThermalStatusLevel {
            return entries.firstOrNull { it.intValue == value } ?: NONE
        }
    }
}

data class ThermalStatusInfo(
    val level: ThermalStatusLevel = ThermalStatusLevel.NONE,
    val isThrottling: Boolean = false,
    val displaySummary: String = "Nominal / Cool"
)

data class BatteryStatusInfo(
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 34.0f
)

data class CpuStatusInfo(
    val requestState: String = "INACTIVE",
    val hintSessionActive: Boolean = false,
    val targetDurationNanos: Long = 1_000_000L,
    val powerStabilityPercent: Int = 0,
    val statusDetail: String = "Ready"
)

data class GpuStatusInfo(
    val requestState: String = "INACTIVE",
    val gpuLockedDutyPercent: Int = 0,
    val statusDetail: String = "Idle"
)

data class DeviceCompatibilityReport(
    val isVivoOrIqoo: Boolean = true,
    val isRealVivoOrIqooHardware: Boolean = true,
    val isVivoIqooSimulationActive: Boolean = false,
    val deviceModel: String = "Device"
)

data class HardwareIndicatorItem(
    val label: String,
    val value: String,
    val subtext: String,
    val isWarning: Boolean = false
)

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isPreloaded: Boolean = false
)

data class PerformanceTelemetryState(
    val isSessionActive: Boolean = false,
    val foregroundServiceRunning: Boolean = false,
    val selectedProfile: PerformanceProfile = PerformanceProfile.DIABLO_MODE,
    val selectedWorkloadFocus: WorkloadFocus = WorkloadFocus.COMBINED_MAX,
    val activeGamePackage: String? = null,
    val activeGameName: String? = null,
    val deviceSpecs: DeviceLiveSpecs = DeviceLiveSpecs(),
    val compatibilityReport: DeviceCompatibilityReport = DeviceCompatibilityReport(),
    val thermalStatus: ThermalStatusInfo = ThermalStatusInfo(),
    val batteryStatus: BatteryStatusInfo = BatteryStatusInfo(),
    val cpuStatus: CpuStatusInfo = CpuStatusInfo(),
    val gpuStatus: GpuStatusInfo = GpuStatusInfo(),
    val isDiabloModeActive: Boolean = false,
    val lockedPowerPercent: Int = 0,
    val gpuLockedDutyPercent: Int = 0,
    val realMeasuredThreadDutyPercent: Int = 0,
    val powerStabilityHistory: List<Int> = List(24) { 0 },
    val gpuStabilityHistory: List<Int> = List(24) { 0 },
    val sessionStartEpochMs: Long = 0L,
    val sessionElapsedSeconds: Long = 0L,
    val recentTelemetryLogLines: List<String> = emptyList(),
    val liveMeasuredFps: Int = 144,
    val liveFrameTimeMs: Float = 6.9f,
    val memoryBandwidthMbPerSec: Int = 0,
    val crc32AluOpsPerSecMillions: Int = 0,
    val gpuFlopOverdriveGflops: Int = 0,
    val audioFastPathActive: Boolean = false,
    val sensorSamplingHz: Int = 0,
    val storageThroughputMbPerSec: Int = 0,
    val wakeLockHeld: Boolean = false,
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
    val nativeCanvasBoundariesMaintained: Boolean = true
)
