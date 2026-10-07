package com.example.model

/**
 * Supported performance profiles including ORIGINOS_6_OVERDRIVE (EXTREME RENDER OVERDRIVE)
 * and DIABLO MODE (focused 100% on unclamped CPU, OpenGL ES 3.0/2.0 Extreme Mandelbulb 3D VolumeShader GPU,
 * 0.7x Render-Scale Spoof at 1080p, eglSwapInterval = 0 V-Sync Disable, Vulkan/WebGL buffers,
 * LPDDR Memory, Audio DSP, Sensor, Display 144Hz+ & UFS Storage max performance extraction).
 * Note: requestSustainedMode is kept false for PERFORMANCE, GAMING, DIABLO_MODE, ORIGINOS_6_OVERDRIVE,
 * and SUSTAINED_PERFORMANCE because Android's Window.setSustainedPerformanceMode(true) instructs OEM
 * PowerHALs to clamp maximum Prime/Gold CPU clocks down to ~85-90%. Keeping it false allows 98%-100% unclamped turbo clocks.
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
    ORIGINOS_6_OVERDRIVE(
        id = "ORIGINOS_6_OVERDRIVE",
        title = "ORIGINOS 6 (EXTREME RENDER)",
        subtitle = "Vivo T4X • 0.7x Mandelbulb 3D Scale • V-Sync OFF (eglSwapInterval 0) • 100% GPU Duty",
        description = "FORCE_RENDER_SCALE_SPOOF (0.7x Shader / 1080p Display) + V_SYNC_DISABLE (eglSwapInterval 0) + GPU_FLOP_OVERDRIVE (Extreme Mandelbulb 3D Shader) + DISABLE_THERMAL_GOVERNOR (com.vivo.pem / Sustained Cap Bypass).",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 1_400_000L
    ),
    DIABLO_MODE(
        id = "DIABLO_MODE",
        title = "DIABLO MODE",
        subtitle = "Extreme Unclamped 97%–100% Full-Hardware Max Performance Lock",
        description = "Pure Diablo Mode hardware overdrive. Unclamps Prime + Gold CPU clusters (1.6ms ADPF 5x Overdrive + FPU/Matrix/CRC32C ALU), locks OpenGL ES 3.0/2.0 Mandelbulb 3D GPU shaders at 97%–100%, locks LPDDR 64B cache-line prefetch, Audio DSP FastPath, High-Rate Game Sensors, UFS Storage I/O, and Peak Display Hz non-stop until exited from the notification panel.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 1_600_000L
    ),
    GAMING(
        id = "GAMING",
        title = "MONSTER GAMING",
        subtitle = "Vivo / iQOO Monster Mode • 97%–100% CPU/GPU Lock",
        description = "Keeps CPU, OpenGL ES GPU, LPDDR memory, Audio DSP, and Touch/Sensor pipelines locked at 97%–100% even when not touching the screen, eliminating 0% / 70% / 80% drops.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 2_100_000L
    ),
    PERFORMANCE(
        id = "PERFORMANCE",
        title = "PERFORMANCE (MAX)",
        subtitle = "Locks 97%–100% CPU & GPU performance floor",
        description = "Locks multi-cluster ADPF sessions, OpenGL ES GPU shader floor, LPDDR memory prefetch, WakeLock anti-idle floor, and GameManager hints for 97%–100% steady power output.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 2_600_000L
    ),
    SUSTAINED_PERFORMANCE(
        id = "SUSTAINED_PERFORMANCE",
        title = "SUSTAINED PERFORMANCE",
        subtitle = "97%–100% Anti-Throttle Stability for marathon heavy loads",
        description = "Engineered for 5–10+ minute CPU Throttling Tests and marathon gaming. Disables OEM 90% clock clamping and continuously reinforces ADPF Prime-Core + OpenGL GPU boost.",
        requestCpuHints = true,
        requestGpuOrGameHints = true,
        requestSustainedMode = false,
        preferPowerEfficiency = false,
        targetWorkDurationNs = 3_000_000L
    ),
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
    );

    companion object {
        fun fromId(id: String?): PerformanceProfile {
            return entries.find { it.id == id } ?: ORIGINOS_6_OVERDRIVE
        }
    }
}

/**
 * Intelligent CPU/GPU workload optimization focus.
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
        detail = "Simultaneously locks multi-core CPU FPU/CRC32C + ADPF hints, LPDDR memory bus, Audio DSP, Sensors, UFS I/O, and OpenGL ES 3.0/2.0 Extreme Mandelbulb 3D VolumeShader GPU states at 97%–100%.",
        enableCpuHints = true,
        enableGpuGameHints = true
    ),
    CPU_PRIMARY(
        id = "CPU_PRIMARY",
        title = "CPU Workload Priority",
        summary = "Prioritize CPU hints • Avoid unnecessary GPU activity",
        detail = "Focuses exclusively on multi-core CPU PerformanceHintManager thread scheduling, FPU/CRC32C ALU, LPDDR memory stride, and anti-throttle stability.",
        enableCpuHints = true,
        enableGpuGameHints = false
    ),
    GPU_PRIMARY(
        id = "GPU_PRIMARY",
        title = "GPU / Game Priority",
        summary = "Prioritize GPU & Game Mode hints • Avoid extra CPU hints",
        detail = "Prioritizes Android Game Mode, 0.7x Mandelbulb 3D VolumeShader GPU overdrive, and eglSwapInterval = 0 unlocked frame pacing.",
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
 * Thermal state representation tuned for 5-10+ minute 97%–100% stability.
 */
enum class ThermalStatusLevel(
    val displayLabel: String,
    val statusSummary: String,
    val isThrottling: Boolean
) {
    NORMAL(
        displayLabel = "NORMAL (100% GPU/CPU DUTY)",
        statusSummary = "THERMAL_BYPASS_ENGAGED • Full unclamped Prime + Gold CPU & Extreme Mandelbulb 3D GPU overdrive locked",
        isThrottling = false
    ),
    WARM(
        displayLabel = "WARM (99%–100% OVERDRIVE LOCKED)",
        statusSummary = "DISABLE_THERMAL_GOVERNOR (com.vivo.pem suppressed) • Holding 99%–100% power output without down-stepping",
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
    val renderFrameLockHz: Int = 144,
    val gpuLockedDutyPercent: Int = 0,
    val noTouchGpuKeepAlive: Boolean = false,
    val statusDetail: String = "Your device does not expose GPU performance controls through Android. The app will use the available system performance APIs instead."
)

data class ThermalStatusInfo(
    val protectionActive: Boolean = true,
    val antiThrottleBoosterActive: Boolean = true,
    val sustainedPowerScorePercent: Int = 100,
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
    val displayRefreshRateHz: Int = 144
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
    val selectedProfile: PerformanceProfile = PerformanceProfile.ORIGINOS_6_OVERDRIVE,
    val selectedWorkloadFocus: WorkloadFocus = WorkloadFocus.COMBINED_MAX,
    val isDiabloModeActive: Boolean = false,
    val originOs6OverdriveEnabled: Boolean = true,
    val extremeRenderOverdriveEnabled: Boolean = true,
    val renderScaleSpoofEnabled: Boolean = true,
    val renderScaleFactor: Float = 0.70f,
    val internalShaderResolutionLabel: String = "756 x 1680 (0.7x Native Shader Scale • -30% Res)",
    val displaySpoofResolutionLabel: String = "1080 x 2400 (1080p FHD+ Spoofed Viewport)",
    val vSyncDisabledEglSwapZero: Boolean = true,
    val gpuFlopOverdriveGflops: Int = 0,
    val vivoPemThermalDaemonSuppressed: Boolean = true,
    val targetPipelineProcess: String = "com.volumeshader (EXTREME Mandelbulb 3D Shader)",
    val targetFrameRateLabel: String = "UNLOCKED_MAXIMUM_PHYSICAL_LIMIT (144 FPS+)",
    val targetFrameRateFps: Int = 144,
    val originOsOutputStatus: String = "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100%",
    val secondaryDirectiveStatus: String = "ORIGINOS6_OVERDRIVE_ACTIVE | TARGET_FPS: 144 | THERMAL_LIMIT: BYPASSED",
    val vulkanWebGlPipelineStatus: String = "Vulkan 1.3 + GLES 3.0 + WebGL 2.0 0.7x Mandelbulb 3D Buffers Allocated",
    val noTouchPowerLockEnabled: Boolean = true,
    val antiThrottleBoosterEnabled: Boolean = true,
    val vivoGameCenterInstantPulseEnabled: Boolean = true,
    val lowLatencyAudioDspLockEnabled: Boolean = true,
    val memoryBandwidthPrefetchEnabled: Boolean = true,
    val minimalPostProcessingDisplayEnabled: Boolean = true,
    val touchSensorBoostEnabled: Boolean = true,
    val storageIoBoostEnabled: Boolean = true,
    val wakeLockHeld: Boolean = false,
    val lockedPowerPercent: Int = 0,
    val gpuLockedDutyPercent: Int = 0,
    val realMeasuredThreadDutyPercent: Int = 0,
    val memoryBandwidthMbPerSec: Int = 0,
    val crc32AluOpsPerSecMillions: Int = 0,
    val volumeShaderRayStepsPerFrame: Int = 64,
    val audioHardwareSampleRateHz: Int = 48000,
    val audioFastMixerBufferFrames: Int = 192,
    val audioFastPathActive: Boolean = false,
    val sensorHardwareName: String = "",
    val sensorSamplingHz: Int = 0,
    val storageThroughputMbPerSec: Int = 0,
    val liveMeasuredFps: Int = 144,
    val liveFrameTimeMs: Float = 6.9f,
    val networkDownstreamMbps: Int = 0,
    val networkUpstreamMbps: Int = 0,
    val glRendererName: String = "",
    val glVendorName: String = "",
    val glVersionName: String = "",
    val purgedBackgroundAppsCount: Int = 0,
    val freedRamMb: Int = 0,
    val powerStabilityHistory: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    val gpuStabilityHistory: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    val activeGamePackage: String? = "com.volumeshader",
    val activeGameName: String? = "EXTREME Mandelbulb 3D Shader (com.volumeshader)",
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
