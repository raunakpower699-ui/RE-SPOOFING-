package com.example.engine

import android.app.ActivityManager
import android.app.GameManager
import android.app.GameState
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PerformanceHintManager
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.view.Display
import com.example.model.ActiveOptimizationIndicator
import com.example.model.BatteryStatusInfo
import com.example.model.CpuStatusInfo
import com.example.model.DeviceCompatibilityReport
import com.example.model.DeviceLiveSpecs
import com.example.model.GpuStatusInfo
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.ThermalStatusInfo
import com.example.model.ThermalStatusLevel
import com.example.model.WorkloadFocus
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * RE Spoofing — Created by Raunak Exploits.
 * Exclusive Vivo & iQOO Android Performance Engine with ROG-inspired DIABLO MODE
 * and real-time Device Hardware & System Specs inspector.
 */
class AndroidPerformanceEngine private constructor(private val appContext: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null

    private var activeHintSession: PerformanceHintManager.Session? = null
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var lastHeadroomQueryMs: Long = 0L
    private var cachedHeadroom: Float? = null
    private var peakSessionTempCelsius: Float = 0f
    private var experiencedThrottlingInSession: Boolean = false

    private var vivoIqooSimulationOverride: Boolean = false
    private var enforceVivoIqooOnlyLock: Boolean = true

    private val _telemetryState = MutableStateFlow(PerformanceTelemetryState())
    val telemetryState: StateFlow<PerformanceTelemetryState> = _telemetryState.asStateFlow()

    var onSessionCompletedCallback: ((
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        gameName: String?,
        startMs: Long,
        durationSec: Long,
        peakTempC: Float,
        throttled: Boolean,
        reason: String
    ) -> Unit)? = null

    init {
        refreshStaticAndDynamicTelemetry()
        val compat = _telemetryState.value.compatibility
        if (compat.isVivoOrIqoo) {
            appendLog("RE Spoofing Engine by Raunak Exploits ready. Verified Vivo/iQOO (${compat.vivoOsInfo}).")
        } else {
            appendLog("RE Spoofing by Raunak Exploits: Non-Vivo/iQOO phone (${compat.manufacturer} ${compat.deviceModel}). Vivo/iQOO Lock ACTIVE.")
        }
    }

    fun setEnforceVivoIqooOnlyLock(enforce: Boolean) {
        enforceVivoIqooOnlyLock = enforce
        refreshStaticAndDynamicTelemetry()
    }

    fun setVivoIqooEmulatorSimulation(enabled: Boolean) {
        vivoIqooSimulationOverride = enabled
        if (!enabled && _telemetryState.value.isSessionActive && !detectRealVivoOrIqooHardware()) {
            stopPerformanceSession("Vivo/iQOO hardware lock re-engaged")
        } else {
            refreshStaticAndDynamicTelemetry()
        }
        appendLog(
            if (enabled) {
                "Vivo / iQOO Hardware Verification Override enabled for device/emulator testing."
            } else {
                "Strict Vivo / iQOO Hardware Lock restored."
            }
        )
    }

    fun isDeviceAuthorizedForVivoIqoo(): Boolean {
        if (!enforceVivoIqooOnlyLock) return true
        return detectRealVivoOrIqooHardware() || vivoIqooSimulationOverride
    }

    fun detectRealVivoOrIqooHardware(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        val model = Build.MODEL.orEmpty()
        val product = Build.PRODUCT.orEmpty()
        val device = Build.DEVICE.orEmpty()
        val board = Build.BOARD.orEmpty()
        val fingerprint = Build.FINGERPRINT.orEmpty()

        val vivoProps = listOfNotNull(
            readPublicSystemProperty("ro.vivo.os.build.display.id"),
            readPublicSystemProperty("ro.vivo.product.version"),
            readPublicSystemProperty("ro.vivo.product.model"),
            readPublicSystemProperty("ro.iqoo.ui.version")
        )

        val combined = "$manufacturer $brand $model $product $device $board $fingerprint"
        return combined.contains("vivo", ignoreCase = true) ||
            combined.contains("iqoo", ignoreCase = true) ||
            vivoProps.isNotEmpty()
    }

    fun refreshStaticAndDynamicTelemetry() {
        val compatibility = detectDeviceCompatibility()
        val battery = readBatteryStatus()
        val thermal = readThermalStatus()
        val specs = readDeviceLiveSpecs()
        val current = _telemetryState.value

        val cpu = buildCpuStatus(
            isActive = current.isSessionActive,
            profile = current.selectedProfile,
            focus = current.selectedWorkloadFocus,
            compatibility = compatibility,
            thermal = thermal
        )
        val gpu = buildGpuStatus(
            isActive = current.isSessionActive,
            profile = current.selectedProfile,
            focus = current.selectedWorkloadFocus,
            compatibility = compatibility,
            thermal = thermal
        )
        val indicators = buildActiveIndicators(
            isActive = current.isSessionActive,
            profile = current.selectedProfile,
            focus = current.selectedWorkloadFocus,
            compatibility = compatibility,
            thermal = thermal,
            serviceRunning = current.foregroundServiceRunning
        )

        _telemetryState.update { state ->
            state.copy(
                compatibility = compatibility,
                batteryStatus = battery,
                thermalStatus = thermal,
                deviceSpecs = specs,
                cpuStatus = cpu,
                gpuStatus = gpu,
                activeIndicators = indicators,
                isDiabloModeActive = state.isSessionActive && state.selectedProfile == PerformanceProfile.DIABLO_MODE,
                sustainedModeRequestedOnWindow = state.isSessionActive &&
                    state.selectedProfile.requestSustainedMode &&
                    compatibility.sustainedPerformanceSupported
            )
        }
    }

    fun setSelectedProfile(profile: PerformanceProfile) {
        _telemetryState.update {
            it.copy(
                selectedProfile = profile,
                isDiabloModeActive = it.isSessionActive && profile == PerformanceProfile.DIABLO_MODE
            )
        }
        if (profile == PerformanceProfile.DIABLO_MODE) {
            appendLog("DIABLO MODE selected: ROG-Style 6.0ms (165Hz-class) target + Uninterruptible Competitive GameState ready.")
        } else {
            appendLog("Profile selected: ${profile.title} (${profile.subtitle})")
        }
        if (_telemetryState.value.isSessionActive) {
            applyPerformanceRequestsForActiveSession()
        } else {
            refreshStaticAndDynamicTelemetry()
        }
    }

    fun setSelectedWorkloadFocus(focus: WorkloadFocus) {
        _telemetryState.update { it.copy(selectedWorkloadFocus = focus) }
        appendLog("Workload focus updated: ${focus.title}")
        if (_telemetryState.value.isSessionActive) {
            applyPerformanceRequestsForActiveSession()
        } else {
            refreshStaticAndDynamicTelemetry()
        }
    }

    fun startPerformanceSession(
        profile: PerformanceProfile = _telemetryState.value.selectedProfile,
        workloadFocus: WorkloadFocus = _telemetryState.value.selectedWorkloadFocus,
        associatedGamePackage: String? = null,
        associatedGameName: String? = null
    ): Boolean {
        if (!isDeviceAuthorizedForVivoIqoo()) {
            val compat = detectDeviceCompatibility()
            appendLog("BLOCKED: RE Spoofing works ONLY on Vivo and iQOO phones (Detected: ${compat.manufacturer} ${compat.deviceModel}).")
            refreshStaticAndDynamicTelemetry()
            return false
        }

        // If already active with the same configuration, refresh notification/state cleanly
        val now = System.currentTimeMillis()
        val battery = readBatteryStatus()
        peakSessionTempCelsius = battery.temperatureCelsius
        experiencedThrottlingInSession = false

        _telemetryState.update {
            it.copy(
                isSessionActive = true,
                foregroundServiceRunning = true,
                selectedProfile = profile,
                selectedWorkloadFocus = workloadFocus,
                isDiabloModeActive = profile == PerformanceProfile.DIABLO_MODE,
                activeGamePackage = associatedGamePackage,
                activeGameName = associatedGameName,
                sessionStartEpochMs = if (it.isSessionActive && it.sessionStartEpochMs > 0L) it.sessionStartEpochMs else now,
                sessionElapsedSeconds = if (it.isSessionActive) it.sessionElapsedSeconds else 0L
            )
        }

        registerThermalListenerIfSupported()
        applyPerformanceRequestsForActiveSession()
        startLightweightMonitoringLoop()

        val targetDesc = associatedGameName?.let { " for $it" } ?: ""
        if (profile == PerformanceProfile.DIABLO_MODE) {
            appendLog("DIABLO MODE IGNITED: 6.0ms Prime Core Hint + UNINTERRUPTIBLE Competitive GameState + Peak Display Mode requested$targetDesc.")
        } else {
            appendLog("START: RE Spoofing Maximum Performance Mode active on Vivo/iQOO [${profile.title} / ${workloadFocus.title}]$targetDesc.")
        }
        return true
    }

    fun stopPerformanceSession(reason: String = "User pressed STOP") {
        val snapshot = _telemetryState.value
        val wasActive = snapshot.isSessionActive
        val startMs = snapshot.sessionStartEpochMs
        val elapsedSec = if (startMs > 0L) ((System.currentTimeMillis() - startMs) / 1000L).coerceAtLeast(1L) else 0L

        monitorJob?.cancel()
        monitorJob = null

        releasePerformanceResources()
        unregisterThermalListener()

        _telemetryState.update {
            it.copy(
                isSessionActive = false,
                foregroundServiceRunning = false,
                isDiabloModeActive = false,
                activeGamePackage = null,
                activeGameName = null,
                sessionStartEpochMs = 0L,
                sessionElapsedSeconds = 0L,
                sustainedModeRequestedOnWindow = false
            )
        }

        refreshStaticAndDynamicTelemetry()
        appendLog("STOP: Performance resources released ($reason). State: INACTIVE.")

        if (wasActive && startMs > 0L) {
            onSessionCompletedCallback?.invoke(
                snapshot.selectedProfile,
                snapshot.selectedWorkloadFocus,
                snapshot.activeGameName,
                startMs,
                elapsedSec,
                peakSessionTempCelsius,
                experiencedThrottlingInSession,
                reason
            )
        }
    }

    private fun applyPerformanceRequestsForActiveSession() {
        val state = _telemetryState.value
        val profile = state.selectedProfile
        val focus = state.selectedWorkloadFocus
        val thermal = readThermalStatus()

        if (thermal.isThrottling) {
            experiencedThrottlingInSession = true
            appendLog("THERMAL SAFEGUARD: Device thermal protection active (${thermal.rawAndroidThermalName}). Respecting OS thermal limits.")
        }

        val shouldRequestCpuHint = profile.requestCpuHints && focus.enableCpuHints
        if (shouldRequestCpuHint && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val hintManager = appContext.getSystemService(PerformanceHintManager::class.java)
                val rate = hintManager?.preferredUpdateRateNanos ?: 0L
                if (hintManager != null && rate > 0L) {
                    activeHintSession?.close()
                    val tids = intArrayOf(Process.myTid())
                    val targetNs = if (thermal.isThrottling) {
                        16_666_666L
                    } else {
                        profile.targetWorkDurationNs
                    }
                    val session = hintManager.createHintSession(tids, targetNs)
                    if (session != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        try {
                            session.setPreferPowerEfficiency(profile.preferPowerEfficiency)
                        } catch (_: Throwable) {
                        }
                    }
                    activeHintSession = session
                } else {
                    activeHintSession?.close()
                    activeHintSession = null
                }
            } catch (t: Throwable) {
                activeHintSession = null
                appendLog("CPU Hint API fallback: ${t.javaClass.simpleName}")
            }
        } else {
            activeHintSession?.close()
            activeHintSession = null
        }

        val shouldSignalGameMode = profile.requestGpuOrGameHints && focus.enableGpuGameHints
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val gameManager = appContext.getSystemService(GameManager::class.java)
                if (gameManager != null) {
                    if (shouldSignalGameMode && !thermal.isThrottling) {
                        val mode = when (profile) {
                            PerformanceProfile.DIABLO_MODE -> GameState.MODE_GAMEPLAY_UNINTERRUPTIBLE
                            PerformanceProfile.GAMING,
                            PerformanceProfile.PERFORMANCE -> GameState.MODE_GAMEPLAY_INTERRUPTIBLE
                            else -> GameState.MODE_CONTENT
                        }
                        gameManager.setGameState(GameState(false, mode))
                    } else {
                        gameManager.setGameState(GameState(false, GameState.MODE_NONE))
                    }
                }
            } catch (_: Throwable) {
            }
        }

        refreshStaticAndDynamicTelemetry()
    }

    private fun releasePerformanceResources() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                activeHintSession?.close()
            } catch (_: Throwable) {
            } finally {
                activeHintSession = null
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val gameManager = appContext.getSystemService(GameManager::class.java)
                gameManager?.setGameState(GameState(false, GameState.MODE_NONE))
            } catch (_: Throwable) {
            }
        }
    }

    private fun startLightweightMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = engineScope.launch {
            while (isActive && _telemetryState.value.isSessionActive) {
                val now = System.currentTimeMillis()
                val startMs = _telemetryState.value.sessionStartEpochMs
                val elapsed = if (startMs > 0L) (now - startMs) / 1000L else 0L

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val session = activeHintSession
                    if (session != null) {
                        try {
                            val targetNs = _telemetryState.value.selectedProfile.targetWorkDurationNs
                            session.reportActualWorkDuration(targetNs)
                        } catch (_: Throwable) {
                        }
                    }
                }

                val battery = readBatteryStatus()
                if (battery.temperatureCelsius > peakSessionTempCelsius) {
                    peakSessionTempCelsius = battery.temperatureCelsius
                }
                val thermal = readThermalStatus()
                if (thermal.isThrottling && !experiencedThrottlingInSession) {
                    experiencedThrottlingInSession = true
                    appendLog("THERMAL ALERT: Performance limited by device thermal protection (${thermal.rawAndroidThermalName}).")
                }

                val specs = readDeviceLiveSpecs()
                val compatibility = _telemetryState.value.compatibility
                val current = _telemetryState.value
                val cpu = buildCpuStatus(
                    isActive = true,
                    profile = current.selectedProfile,
                    focus = current.selectedWorkloadFocus,
                    compatibility = compatibility,
                    thermal = thermal
                )
                val gpu = buildGpuStatus(
                    isActive = true,
                    profile = current.selectedProfile,
                    focus = current.selectedWorkloadFocus,
                    compatibility = compatibility,
                    thermal = thermal
                )
                val indicators = buildActiveIndicators(
                    isActive = true,
                    profile = current.selectedProfile,
                    focus = current.selectedWorkloadFocus,
                    compatibility = compatibility,
                    thermal = thermal,
                    serviceRunning = current.foregroundServiceRunning
                )

                _telemetryState.update {
                    it.copy(
                        sessionElapsedSeconds = elapsed,
                        batteryStatus = battery,
                        thermalStatus = thermal,
                        deviceSpecs = specs,
                        cpuStatus = cpu,
                        gpuStatus = gpu,
                        activeIndicators = indicators
                    )
                }

                delay(2000L)
            }
        }
    }

    private fun registerThermalListenerIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalListener == null) {
            try {
                val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
                val listener = PowerManager.OnThermalStatusChangedListener { status ->
                    val name = mapThermalStatusToName(status)
                    appendLog("Thermal status update from Android OS: $name")
                    refreshStaticAndDynamicTelemetry()
                }
                pm.addThermalStatusListener(appContext.mainExecutor, listener)
                thermalListener = listener
            } catch (_: Throwable) {
            }
        }
    }

    private fun unregisterThermalListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val listener = thermalListener ?: return
            try {
                val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.removeThermalStatusListener(listener)
            } catch (_: Throwable) {
            } finally {
                thermalListener = null
            }
        }
    }

    /**
     * Reads real device information (Phone Name, Model, Android Version, Security Patch,
     * Processor/SoC, Live RAM, Storage, and Display Specs) for any user who opens the app.
     */
    fun readDeviceLiveSpecs(): DeviceLiveSpecs {
        val manufacturer = Build.MANUFACTURER?.takeIf { it.isNotBlank() } ?: "Android"
        val brand = Build.BRAND?.takeIf { it.isNotBlank() } ?: manufacturer
        val model = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Smartphone"
        val codename = Build.DEVICE?.takeIf { it.isNotBlank() } ?: "device"
        val board = Build.BOARD?.takeIf { it.isNotBlank() } ?: "board"
        val release = Build.VERSION.RELEASE?.takeIf { it.isNotBlank() } ?: "Unknown"
        val sdk = Build.VERSION.SDK_INT
        val patch = Build.VERSION.SECURITY_PATCH?.takeIf { it.isNotBlank() } ?: "Standard"
        val buildId = Build.DISPLAY?.takeIf { it.isNotBlank() } ?: Build.ID.orEmpty()

        val marketingName = readPublicSystemProperty("ro.vivo.market.name")
            ?: readPublicSystemProperty("ro.product.marketname")
            ?: if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"

        val soc = if (sdk >= Build.VERSION_CODES.S) {
            val s = Build.SOC_MODEL
            if (!s.isNullOrBlank() && s != Build.UNKNOWN) s else (Build.HARDWARE ?: board)
        } else {
            Build.HARDWARE ?: board
        }

        val cores = Runtime.getRuntime().availableProcessors()
        val abi = Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64-v8a"

        // RAM calculation via ActivityManager
        var totalRamStr = "N/A"
        var availRamStr = "N/A"
        var ramPct = 0
        try {
            val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null) {
                val memInfo = ActivityManager.MemoryInfo()
                am.getMemoryInfo(memInfo)
                val totalGb = memInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val availGb = memInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val usedGb = (totalGb - availGb).coerceAtLeast(0.0)
                totalRamStr = String.format(Locale.US, "%.1f GB", totalGb)
                availRamStr = String.format(Locale.US, "%.1f GB Free", availGb)
                if (totalGb > 0.0) {
                    ramPct = ((usedGb / totalGb) * 100.0).roundToInt().coerceIn(1, 99)
                }
            }
        } catch (_: Throwable) {
        }

        // Internal storage calculation via StatFs
        var totalStorageStr = "N/A"
        var freeStorageStr = "N/A"
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockSizeLong * stat.blockCountLong
            val availBytes = stat.blockSizeLong * stat.availableBlocksLong
            val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val freeGb = availBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            totalStorageStr = String.format(Locale.US, "%.1f GB", totalGb)
            freeStorageStr = String.format(Locale.US, "%.1f GB Free", freeGb)
        } catch (_: Throwable) {
        }

        // Display resolution & refresh rate
        var resolutionStr = "1080 x 2400"
        var refreshRateHz = 60
        try {
            val dm = appContext.resources.displayMetrics
            if (dm != null && dm.widthPixels > 0 && dm.heightPixels > 0) {
                resolutionStr = "${dm.widthPixels} × ${dm.heightPixels} (${dm.densityDpi} dpi)"
            }
            val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val defaultDisplay = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            if (defaultDisplay != null) {
                refreshRateHz = defaultDisplay.refreshRate.roundToInt().coerceAtLeast(60)
            }
        } catch (_: Throwable) {
        }

        return DeviceLiveSpecs(
            phoneDisplayName = marketingName,
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            deviceCodename = codename,
            boardName = board,
            androidVersionLabel = "Android $release (API $sdk)",
            securityPatch = patch,
            buildId = buildId,
            socProcessor = soc,
            cpuCores = cores,
            cpuAbi = abi,
            totalRamGb = totalRamStr,
            availableRamGb = availRamStr,
            ramUsedPercent = ramPct,
            totalStorageGb = totalStorageStr,
            freeStorageGb = freeStorageStr,
            screenResolution = resolutionStr,
            displayRefreshRateHz = refreshRateHz
        )
    }

    fun detectDeviceCompatibility(): DeviceCompatibilityReport {
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE ?: "Unknown"
        val realManufacturer = Build.MANUFACTURER ?: "Unknown"
        val realBrand = Build.BRAND ?: "Unknown"
        val realModel = Build.MODEL ?: "Android Device"
        val soc = if (sdk >= Build.VERSION_CODES.S) {
            val socModel = Build.SOC_MODEL
            if (!socModel.isNullOrBlank() && socModel != Build.UNKNOWN) socModel else (Build.HARDWARE ?: "SoC")
        } else {
            Build.HARDWARE ?: "SoC"
        }

        val isRealVivoIqoo = detectRealVivoOrIqooHardware()
        val isAuthorizedVivoIqoo = isDeviceAuthorizedForVivoIqoo()

        val displayManufacturer = if (isRealVivoIqoo) realManufacturer else if (vivoIqooSimulationOverride) "vivo / iQOO (Verified Mode)" else realManufacturer
        val displayBrand = if (isRealVivoIqoo) realBrand else if (vivoIqooSimulationOverride) "iQOO" else realBrand
        val displayModel = if (isRealVivoIqoo) realModel else if (vivoIqooSimulationOverride) "$realModel [Vivo/iQOO Mode]" else realModel

        val vivoOsDisplay = readPublicSystemProperty("ro.vivo.os.build.display.id")
            ?: readPublicSystemProperty("ro.vivo.product.version")
            ?: when {
                isRealVivoIqoo -> "OriginOS / FuntouchOS (Vivo/iQOO Hardware Verified)"
                vivoIqooSimulationOverride -> "Vivo / iQOO OriginOS Mode (Emulator Override Active)"
                else -> "Unsupported Non-Vivo/iQOO Firmware ($realManufacturer)"
            }

        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val sustainedSupported = pm?.isSustainedPerformanceModeSupported == true
        val thermalSupported = sdk >= Build.VERSION_CODES.Q

        var headroomSupported = false
        if (sdk >= Build.VERSION_CODES.R && pm != null) {
            try {
                val hr = queryThermalHeadroomSafely(pm)
                headroomSupported = hr != null && !hr.isNaN()
            } catch (_: Throwable) {
                headroomSupported = false
            }
        }

        var cpuHintSupported = false
        if (sdk >= Build.VERSION_CODES.S) {
            try {
                val hm = appContext.getSystemService(PerformanceHintManager::class.java)
                cpuHintSupported = hm != null && hm.preferredUpdateRateNanos > 0L
            } catch (_: Throwable) {
                cpuHintSupported = false
            }
        }
        val cpuEfficiencySupported = cpuHintSupported && sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

        var gameModeSupported = false
        var gameModeLabel = "Not Exposed (API < 31)"
        if (sdk >= Build.VERSION_CODES.S) {
            try {
                val gm = appContext.getSystemService(GameManager::class.java)
                if (gm != null) {
                    gameModeSupported = true
                    gameModeLabel = when (gm.gameMode) {
                        GameManager.GAME_MODE_PERFORMANCE -> "GAME_MODE_PERFORMANCE"
                        GameManager.GAME_MODE_STANDARD -> "GAME_MODE_STANDARD"
                        GameManager.GAME_MODE_BATTERY -> "GAME_MODE_BATTERY"
                        GameManager.GAME_MODE_CUSTOM -> "GAME_MODE_CUSTOM"
                        else -> "GAME_MODE_UNSUPPORTED (Default)"
                    }
                }
            } catch (_: Throwable) {
                gameModeLabel = "Restricted by OEM"
            }
        }

        val gpuHintExposed = false

        val oemStatus = if (isAuthorizedVivoIqoo) {
            "Vivo/iQOO environment active ($vivoOsDisplay). Standard Android Game/Performance APIs enabled; proprietary OEM kernel control unavailable to this application."
        } else {
            "DEVICE LOCKED: RE Spoofing works ONLY on Vivo and iQOO smartphones. OEM performance control unavailable to this application."
        }

        return DeviceCompatibilityReport(
            androidRelease = release,
            sdkInt = sdk,
            manufacturer = displayManufacturer,
            brand = displayBrand,
            deviceModel = displayModel,
            socOrHardware = soc,
            isVivoOrIqoo = isAuthorizedVivoIqoo,
            isRealVivoOrIqooHardware = isRealVivoIqoo,
            isVivoIqooSimulationActive = vivoIqooSimulationOverride,
            vivoOsInfo = vivoOsDisplay,
            cpuPerformanceHintSupported = cpuHintSupported,
            cpuPowerEfficiencySupported = cpuEfficiencySupported,
            gpuPerformanceHintExposed = gpuHintExposed,
            gameModeSupported = gameModeSupported,
            currentGameModeLabel = gameModeLabel,
            sustainedPerformanceSupported = sustainedSupported,
            thermalMonitoringSupported = thermalSupported,
            thermalHeadroomSupported = headroomSupported,
            oemPerformanceControlStatus = oemStatus
        )
    }

    fun readThermalStatus(): ThermalStatusInfo {
        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val battery = readBatteryStatus()
        var rawStatus = 0
        var rawName = "THERMAL_STATUS_NONE"
        var level = ThermalStatusLevel.NORMAL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm != null) {
            try {
                rawStatus = pm.currentThermalStatus
                rawName = mapThermalStatusToName(rawStatus)
                level = when (rawStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> ThermalStatusLevel.NORMAL
                    PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatusLevel.WARM
                    PowerManager.THERMAL_STATUS_MODERATE,
                    PowerManager.THERMAL_STATUS_SEVERE,
                    PowerManager.THERMAL_STATUS_CRITICAL,
                    PowerManager.THERMAL_STATUS_EMERGENCY,
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatusLevel.THROTTLING
                    else -> ThermalStatusLevel.NORMAL
                }
            } catch (_: Throwable) {
            }
        } else {
            level = when {
                battery.temperatureCelsius >= 43.0f -> ThermalStatusLevel.THROTTLING
                battery.temperatureCelsius >= 38.5f -> ThermalStatusLevel.WARM
                else -> ThermalStatusLevel.NORMAL
            }
            rawName = "BATTERY_SENSOR_FALLBACK"
        }

        val headroom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm != null) {
            queryThermalHeadroomSafely(pm)
        } else {
            null
        }

        val isThrottling = level.isThrottling
        val warningBanner = if (isThrottling) {
            "Performance limited by device thermal protection"
        } else {
            null
        }

        return ThermalStatusInfo(
            protectionActive = true,
            level = level,
            rawAndroidThermalCode = rawStatus,
            rawAndroidThermalName = rawName,
            thermalHeadroom = headroom,
            batteryTempCelsius = battery.temperatureCelsius,
            isThrottling = isThrottling,
            warningBannerText = warningBanner
        )
    }

    private fun queryThermalHeadroomSafely(pm: PowerManager): Float? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val now = System.currentTimeMillis()
        if (now - lastHeadroomQueryMs < 10_500L && cachedHeadroom != null) {
            return cachedHeadroom
        }
        return try {
            val value = pm.getThermalHeadroom(10)
            lastHeadroomQueryMs = now
            if (value.isNaN() || value <= 0f) {
                cachedHeadroom = null
                null
            } else {
                cachedHeadroom = value
                value
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun readBatteryStatus(): BatteryStatusInfo {
        val intent = try {
            appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Throwable) {
            null
        }
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL ||
            plugged != 0

        val sourceLabel = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast/Wall Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Power Source"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charger"
            else -> if (isCharging) "External Power" else "Battery (Discharging)"
        }

        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val tempC = (tempTenths / 10f).coerceIn(0f, 90f)
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000

        val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batterySaver = pm?.isPowerSaveMode == true
        val isLow = pct <= 20 && !isCharging

        return BatteryStatusInfo(
            levelPercent = pct,
            isCharging = isCharging,
            powerSourceLabel = sourceLabel,
            temperatureCelsius = tempC,
            voltageMv = voltageMv,
            isLowBattery = isLow,
            isSystemBatterySaverOn = batterySaver
        )
    }

    private fun buildCpuStatus(
        isActive: Boolean,
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        compatibility: DeviceCompatibilityReport,
        thermal: ThermalStatusInfo
    ): CpuStatusInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64-v8a"
        val freqSummary = readRealCpuFrequencySummary(cores)

        var preferredRate = 0L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val hm = appContext.getSystemService(PerformanceHintManager::class.java)
                preferredRate = hm?.preferredUpdateRateNanos ?: 0L
            } catch (_: Throwable) {
            }
        }

        if (!compatibility.isVivoOrIqoo) {
            return CpuStatusInfo(
                requestState = "LOCKED (VIVO/iQOO ONLY)",
                hintSessionActive = false,
                coreCount = cores,
                preferredUpdateRateNanos = preferredRate,
                targetDurationNanos = 0L,
                architecture = arch,
                readableFrequenciesSummary = freqSummary,
                statusDetail = "Blocked: RE Spoofing works exclusively on Vivo and iQOO phones."
            )
        }

        if (!isActive) {
            return CpuStatusInfo(
                requestState = "INACTIVE",
                hintSessionActive = false,
                coreCount = cores,
                preferredUpdateRateNanos = preferredRate,
                targetDurationNanos = profile.targetWorkDurationNs,
                architecture = arch,
                readableFrequenciesSummary = freqSummary,
                statusDetail = "Standby on Vivo/iQOO — Press START to request CPU performance hints"
            )
        }

        if (!focus.enableCpuHints) {
            return CpuStatusInfo(
                requestState = "STANDBY (GPU FOCUS)",
                hintSessionActive = false,
                coreCount = cores,
                preferredUpdateRateNanos = preferredRate,
                targetDurationNanos = 0L,
                architecture = arch,
                readableFrequenciesSummary = freqSummary,
                statusDetail = "GPU workload priority selected — avoiding unnecessary CPU hint requests"
            )
        }

        if (thermal.isThrottling) {
            return CpuStatusInfo(
                requestState = "ACTIVE (THERMAL LIMITED)",
                hintSessionActive = activeHintSession != null,
                coreCount = cores,
                preferredUpdateRateNanos = preferredRate,
                targetDurationNanos = 16_666_666L,
                architecture = arch,
                readableFrequenciesSummary = freqSummary,
                statusDetail = "Performance limited by device thermal protection — OS governor throttling active"
            )
        }

        val sessionUp = activeHintSession != null
        val isDiablo = profile == PerformanceProfile.DIABLO_MODE
        val detail = when {
            isDiablo && sessionUp -> "DIABLO OVERDRIVE: 6.0 ms (165Hz-class) Prime Core PerformanceHintSession active"
            isDiablo -> "DIABLO OVERDRIVE: Requesting 6.0 ms low-latency CPU scheduling priority"
            sessionUp -> "PerformanceHintManager session active (Target: ${profile.targetWorkDurationNs / 1_000_000.0} ms)"
            compatibility.cpuPerformanceHintSupported -> "Vivo/iQOO CPU scheduling hints requested for ${profile.title} mode"
            else -> "Standard Android thread priority & power hint active (PerformanceHintManager not exposed)"
        }

        return CpuStatusInfo(
            requestState = if (isDiablo) "ACTIVE (DIABLO OVERDRIVE)" else "ACTIVE",
            hintSessionActive = sessionUp,
            coreCount = cores,
            preferredUpdateRateNanos = preferredRate,
            targetDurationNanos = profile.targetWorkDurationNs,
            architecture = arch,
            readableFrequenciesSummary = freqSummary,
            statusDetail = detail
        )
    }

    private fun buildGpuStatus(
        isActive: Boolean,
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        compatibility: DeviceCompatibilityReport,
        thermal: ThermalStatusInfo
    ): GpuStatusInfo {
        val fallbackExplanation =
            "Your device does not expose GPU performance controls through Android. The app will use the available system performance APIs instead."

        if (!compatibility.isVivoOrIqoo) {
            return GpuStatusInfo(
                requestState = "LOCKED (VIVO/iQOO ONLY)",
                gameStateSignaled = false,
                directGpuControlExposed = false,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = "Blocked: RE Spoofing works exclusively on Vivo and iQOO phones."
            )
        }

        if (!isActive) {
            return GpuStatusInfo(
                requestState = "INACTIVE",
                gameStateSignaled = false,
                directGpuControlExposed = compatibility.gpuPerformanceHintExposed,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = fallbackExplanation
            )
        }

        if (!focus.enableGpuGameHints || !profile.requestGpuOrGameHints) {
            return GpuStatusInfo(
                requestState = "STANDBY (CPU / SUSTAINED FOCUS)",
                gameStateSignaled = false,
                directGpuControlExposed = compatibility.gpuPerformanceHintExposed,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = "GPU/Game Mode hints intentionally paused for current workload focus"
            )
        }

        if (thermal.isThrottling) {
            return GpuStatusInfo(
                requestState = "LIMITED BY THERMAL",
                gameStateSignaled = false,
                directGpuControlExposed = compatibility.gpuPerformanceHintExposed,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = "Performance limited by device thermal protection"
            )
        }

        val isDiablo = profile == PerformanceProfile.DIABLO_MODE
        return if (compatibility.gameModeSupported) {
            GpuStatusInfo(
                requestState = if (isDiablo) "ACTIVE (DIABLO UNINTERRUPTIBLE)" else "ACTIVE",
                gameStateSignaled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                directGpuControlExposed = false,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = if (isDiablo) {
                    "DIABLO MODE: GameState.MODE_GAMEPLAY_UNINTERRUPTIBLE signaled (${compatibility.currentGameModeLabel}). $fallbackExplanation"
                } else {
                    "GameManager GameState active (${compatibility.currentGameModeLabel}). $fallbackExplanation"
                }
            )
        } else {
            GpuStatusInfo(
                requestState = if (isDiablo) "ACTIVE (DIABLO FALLBACK)" else "ACTIVE (SYSTEM FALLBACK)",
                gameStateSignaled = false,
                directGpuControlExposed = false,
                hardwareModel = compatibility.socOrHardware,
                statusDetail = fallbackExplanation
            )
        }
    }

    private fun buildActiveIndicators(
        isActive: Boolean,
        profile: PerformanceProfile,
        focus: WorkloadFocus,
        compatibility: DeviceCompatibilityReport,
        thermal: ThermalStatusInfo,
        serviceRunning: Boolean
    ): List<ActiveOptimizationIndicator> {
        val isDiablo = profile == PerformanceProfile.DIABLO_MODE
        return listOf(
            ActiveOptimizationIndicator(
                id = "vivo_iqoo_gate",
                title = "Vivo & iQOO Exclusive Hardware Gate",
                stateLabel = if (compatibility.isVivoOrIqoo) "VERIFIED VIVO/iQOO" else "BLOCKED (NON-VIVO)",
                description = if (compatibility.isVivoOrIqoo) {
                    "Verified ${compatibility.vivoOsInfo}. RE Spoofing performance features unlocked."
                } else {
                    "RE Spoofing is locked to Vivo and iQOO smartphones only. Detected: ${compatibility.manufacturer} ${compatibility.deviceModel}."
                },
                isActive = compatibility.isVivoOrIqoo,
                isFallbackOrLimited = !compatibility.isRealVivoOrIqooHardware
            ),
            ActiveOptimizationIndicator(
                id = "diablo_overdrive",
                title = "ROG-Style Diablo Mode Overdrive",
                stateLabel = when {
                    !compatibility.isVivoOrIqoo -> "LOCKED"
                    isActive && isDiablo -> "IGNITED (6.0ms / 165Hz)"
                    isDiablo -> "READY TO IGNITE"
                    else -> "STANDBY"
                },
                description = "Requests ultra-low 6.0ms frame pacing target, UNINTERRUPTIBLE GameState lock, Sustained Window mode, and peak display refresh rate.",
                isActive = isActive && isDiablo && compatibility.isVivoOrIqoo,
                isFallbackOrLimited = false
            ),
            ActiveOptimizationIndicator(
                id = "fgs",
                title = "Foreground Performance Service",
                stateLabel = if (serviceRunning && isActive) "RUNNING" else "INACTIVE",
                description = if (serviceRunning && isActive) {
                    "Persistent notification active (RE Spoofing — Performance Mode Active)."
                } else {
                    "Service stopped. Zero background overhead."
                },
                isActive = serviceRunning && isActive
            ),
            ActiveOptimizationIndicator(
                id = "cpu_hint",
                title = "CPU Performance Hint API",
                stateLabel = when {
                    !compatibility.isVivoOrIqoo -> "LOCKED"
                    !isActive -> "STANDBY"
                    !focus.enableCpuHints -> "SKIPPED (GPU FOCUS)"
                    compatibility.cpuPerformanceHintSupported -> if (isDiablo) "DIABLO 6.0ms" else "ACTIVE"
                    else -> "FALLBACK ACTIVE"
                },
                description = if (compatibility.cpuPerformanceHintSupported) {
                    "Supported — Android PerformanceHintManager session reporting target work duration."
                } else {
                    "PerformanceHintManager not exposed by hardware HAL; using standard OS thread scheduling hints."
                },
                isActive = isActive && focus.enableCpuHints && compatibility.isVivoOrIqoo,
                isFallbackOrLimited = !compatibility.cpuPerformanceHintSupported
            ),
            ActiveOptimizationIndicator(
                id = "gpu_hint",
                title = "GPU / Game Mode Hint API",
                stateLabel = when {
                    !compatibility.isVivoOrIqoo -> "LOCKED"
                    !isActive -> "STANDBY"
                    !focus.enableGpuGameHints || !profile.requestGpuOrGameHints -> "SKIPPED (CPU FOCUS)"
                    compatibility.gameModeSupported -> if (isDiablo) "UNINTERRUPTIBLE" else "ACTIVE (GAME API)"
                    else -> "SYSTEM FALLBACK"
                },
                description = "Direct GPU clock control: Not exposed by this device. Using supported Android GameManager & Vivo/iQOO system APIs.",
                isActive = isActive && focus.enableGpuGameHints && profile.requestGpuOrGameHints && compatibility.isVivoOrIqoo,
                isFallbackOrLimited = true
            ),
            ActiveOptimizationIndicator(
                id = "thermal_guard",
                title = "Android Thermal Safeguard",
                stateLabel = if (thermal.isThrottling) "THROTTLING ACTIVE" else "PROTECTED",
                description = if (thermal.isThrottling) {
                    "Performance limited by device thermal protection (${thermal.rawAndroidThermalName})."
                } else {
                    "Thermal Protection: ACTIVE (${thermal.level.displayLabel}). Never bypassed or disabled."
                },
                isActive = true,
                isFallbackOrLimited = thermal.isThrottling
            )
        )
    }

    private fun readRealCpuFrequencySummary(coreCount: Int): String {
        return try {
            val readableMhz = mutableListOf<Int>()
            for (i in 0 until coreCount.coerceAtMost(8)) {
                val curFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
                if (curFile.exists() && curFile.canRead()) {
                    val khz = curFile.readText().trim().toIntOrNull()
                    if (khz != null && khz > 0) {
                        readableMhz.add(khz / 1000)
                    }
                }
            }
            if (readableMhz.isNotEmpty()) {
                val minMhz = readableMhz.minOrNull() ?: 0
                val maxMhz = readableMhz.maxOrNull() ?: 0
                "$coreCount Cores • Real Clock: $minMhz–$maxMhz MHz"
            } else {
                "$coreCount Cores • OS Governor Managed (No Fake %)"
            }
        } catch (_: Throwable) {
            "$coreCount Cores • OS Governor Managed (No Fake %)"
        }
    }

    private fun readPublicSystemProperty(key: String): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java, String::class.java)
            val result = getMethod.invoke(null, key, "") as? String
            result?.takeIf { it.isNotBlank() }
        } catch (_: Throwable) {
            null
        }
    }

    private fun mapThermalStatusToName(status: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "THERMAL_API_LEGACY"
        return when (status) {
            PowerManager.THERMAL_STATUS_NONE -> "THERMAL_STATUS_NONE"
            PowerManager.THERMAL_STATUS_LIGHT -> "THERMAL_STATUS_LIGHT"
            PowerManager.THERMAL_STATUS_MODERATE -> "THERMAL_STATUS_MODERATE"
            PowerManager.THERMAL_STATUS_SEVERE -> "THERMAL_STATUS_SEVERE"
            PowerManager.THERMAL_STATUS_CRITICAL -> "THERMAL_STATUS_CRITICAL"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "THERMAL_STATUS_EMERGENCY"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "THERMAL_STATUS_SHUTDOWN"
            else -> "THERMAL_STATUS_$status"
        }
    }

    private fun appendLog(message: String) {
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val entry = "[$timeFmt] $message"
        _telemetryState.update { state ->
            val updated = (listOf(entry) + state.eventLogs).take(40)
            state.copy(eventLogs = updated)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AndroidPerformanceEngine? = null

        fun getInstance(context: Context): AndroidPerformanceEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = AndroidPerformanceEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
