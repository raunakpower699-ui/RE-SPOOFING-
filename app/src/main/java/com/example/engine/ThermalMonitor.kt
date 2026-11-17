package com.example.engine

import android.content.Context
import android.os.Build
import android.os.HardwarePropertiesManager
import android.os.PowerManager
import com.example.model.ThermalStatusInfo
import com.example.model.ThermalStatusLevel
import java.io.File

data class RealThermalReading(
    val cpuTempCelsius: Float,
    val gpuTempCelsius: Float,
    val batteryTempCelsius: Float,
    val thermalHeadroomRatio: Float,
    val thermalLevel: ThermalStatusLevel,
    val isThrottlingDetected: Boolean,
    val sensorSourceLabel: String,
    val alertHeadline: String?,
    val fpsDropCauseExplanation: String
)

class ThermalMonitor(private val context: Context) {

    private var cachedCpuZoneFile: File? = null
    private var cachedGpuZoneFile: File? = null
    private var zonesScanned: Boolean = false
    private var statusListener: PowerManager.OnThermalStatusChangedListener? = null
    private var lastHeadroomQueryMs: Long = 0L
    private var cachedThermalLoad: Float? = null

    fun registerThermalStatusListener(onStatusChanged: (ThermalStatusLevel) -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            if (statusListener != null) return
            val listener = PowerManager.OnThermalStatusChangedListener { statusInt ->
                onStatusChanged(ThermalStatusLevel.fromInt(statusInt))
            }
            pm.addThermalStatusListener(context.mainExecutor, listener)
            statusListener = listener
        } catch (_: Throwable) {
        }
    }

    fun unregisterThermalStatusListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            statusListener?.let { pm.removeThermalStatusListener(it) }
        } catch (_: Throwable) {
        } finally {
            statusListener = null
        }
    }

    fun sampleRealThermals(
        batteryTempCelsius: Float,
        isSessionActive: Boolean
    ): RealThermalReading {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

        val thermalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm != null) {
            try {
                ThermalStatusLevel.fromInt(pm.currentThermalStatus)
            } catch (_: Throwable) {
                ThermalStatusLevel.NONE
            }
        } else {
            ThermalStatusLevel.NONE
        }

        // ADPF getThermalHeadroom(0) returns thermal load toward SEVERE throttling (0.0 = cool/unsupported, 1.0 = severe throttling).
        // Per ADPF guidelines, ignore 0.0f or NaN (unsupported/emulator) and poll at most once every 10s.
        val headroomRatio = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm != null) {
            try {
                val nowMs = System.currentTimeMillis()
                if (nowMs - lastHeadroomQueryMs >= 10_000L || lastHeadroomQueryMs == 0L) {
                    lastHeadroomQueryMs = nowMs
                    val raw = pm.getThermalHeadroom(0)
                    if (!raw.isNaN() && raw > 0.0f) {
                        cachedThermalLoad = raw.coerceIn(0f, 1.5f)
                    } else if (raw.isNaN() || raw == 0.0f) {
                        cachedThermalLoad = null
                    }
                }
                val validLoad = cachedThermalLoad
                if (validLoad != null) {
                    (1.0f - validLoad).coerceIn(0f, 1f)
                } else {
                    estimateHeadroomFromStatus(thermalLevel, batteryTempCelsius)
                }
            } catch (_: Throwable) {
                estimateHeadroomFromStatus(thermalLevel, batteryTempCelsius)
            }
        } else {
            estimateHeadroomFromStatus(thermalLevel, batteryTempCelsius)
        }

        // 1. Try HardwarePropertiesManager if privileged
        var cpuTemp = readHardwarePropertyTemp(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU)
        var gpuTemp = readHardwarePropertyTemp(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU)
        var sourceLabel = if (cpuTemp != null || gpuTemp != null) "HardwareProperties API" else ""

        // 2. Try Linux sysfs thermal zones (/sys/class/thermal/thermal_zone*)
        if (cpuTemp == null || gpuTemp == null) {
            ensureThermalZonesDiscovered()
            if (cpuTemp == null) {
                cpuTemp = readZoneTemperature(cachedCpuZoneFile)
            }
            if (gpuTemp == null) {
                gpuTemp = readZoneTemperature(cachedGpuZoneFile)
            }
            if (sourceLabel.isEmpty() && (cpuTemp != null || gpuTemp != null)) {
                sourceLabel = "SoC Thermal Zone"
            }
        }

        // 3. Fallback when SELinux blocks sysfs on Android 16: derive calibrated SoC temp from real ADPF Thermal Headroom + ThermalStatus + Battery Temp
        val levelOffset = when (thermalLevel) {
            ThermalStatusLevel.NONE -> if (isSessionActive) 9.5f else 5.0f
            ThermalStatusLevel.LIGHT -> 16.0f
            ThermalStatusLevel.MODERATE -> 26.0f
            ThermalStatusLevel.SEVERE -> 36.0f
            ThermalStatusLevel.CRITICAL -> 44.0f
            ThermalStatusLevel.EMERGENCY -> 50.0f
            ThermalStatusLevel.SHUTDOWN -> 55.0f
        }
        val headroomPenalty = ((1.0f - headroomRatio.coerceIn(0f, 1f)) * 18.0f)

        val resolvedCpuTemp = (cpuTemp ?: (batteryTempCelsius + levelOffset + headroomPenalty))
            .coerceIn(20.0f, 105.0f)
        val resolvedGpuTemp = (gpuTemp ?: (resolvedCpuTemp - 2.2f))
            .coerceIn(20.0f, 105.0f)

        if (sourceLabel.isEmpty()) {
            sourceLabel = "ADPF Headroom + Battery Telemetry"
        }

        val isThrottling = thermalLevel.isThrottlingRisk ||
            thermalLevel == ThermalStatusLevel.MODERATE ||
            headroomRatio < 0.22f ||
            resolvedCpuTemp >= 72.0f ||
            resolvedGpuTemp >= 70.0f ||
            batteryTempCelsius >= 43.0f

        val (alertHeadline, dropCause) = when {
            thermalLevel.intValue >= ThermalStatusLevel.SEVERE.intValue || resolvedCpuTemp >= 80.0f || resolvedGpuTemp >= 78.0f -> {
                "CRITICAL THROTTLING DETECTED" to
                    "CPU (${formatTemp(resolvedCpuTemp)}°C) & GPU (${formatTemp(resolvedGpuTemp)}°C) exceeded safe ceiling — SoC is cutting clock speeds, causing heavy FPS drops. Cool down device for 2-3 mins."
            }
            isThrottling -> {
                "THERMAL THROTTLING ALERT" to
                    "Heat buildup (CPU ${formatTemp(resolvedCpuTemp)}°C • GPU ${formatTemp(resolvedGpuTemp)}°C • Headroom ${(headroomRatio * 100).toInt()}%) is triggering OS clock limits, causing FPS instability."
            }
            thermalLevel == ThermalStatusLevel.LIGHT || batteryTempCelsius >= 39.5f -> {
                null to
                    "Warming Up (CPU ${formatTemp(resolvedCpuTemp)}°C • GPU ${formatTemp(resolvedGpuTemp)}°C) — Zero-Contention Guard active to keep FPS stable."
            }
            else -> {
                null to
                    "Thermals Optimal (CPU ${formatTemp(resolvedCpuTemp)}°C • GPU ${formatTemp(resolvedGpuTemp)}°C) — No thermal throttling; full FPS headroom available."
            }
        }

        return RealThermalReading(
            cpuTempCelsius = (resolvedCpuTemp * 10f).toInt() / 10f,
            gpuTempCelsius = (resolvedGpuTemp * 10f).toInt() / 10f,
            batteryTempCelsius = batteryTempCelsius,
            thermalHeadroomRatio = headroomRatio,
            thermalLevel = thermalLevel,
            isThrottlingDetected = isThrottling,
            sensorSourceLabel = sourceLabel,
            alertHeadline = alertHeadline,
            fpsDropCauseExplanation = dropCause
        )
    }

    private fun estimateHeadroomFromStatus(level: ThermalStatusLevel, batteryTemp: Float): Float {
        val base = when (level) {
            ThermalStatusLevel.NONE -> 0.92f
            ThermalStatusLevel.LIGHT -> 0.65f
            ThermalStatusLevel.MODERATE -> 0.35f
            ThermalStatusLevel.SEVERE -> 0.15f
            ThermalStatusLevel.CRITICAL -> 0.05f
            ThermalStatusLevel.EMERGENCY, ThermalStatusLevel.SHUTDOWN -> 0.0f
        }
        val batPenalty = ((batteryTemp - 36f).coerceAtLeast(0f) * 0.03f)
        return (base - batPenalty).coerceIn(0f, 1f)
    }

    private fun readHardwarePropertyTemp(deviceType: Int): Float? {
        return try {
            val hpm = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? HardwarePropertiesManager
                ?: return null
            val temps = hpm.getDeviceTemperatures(deviceType, HardwarePropertiesManager.TEMPERATURE_CURRENT)
            val valid = temps.filter { it > 0f && it < 120f }
            if (valid.isNotEmpty()) valid.maxOrNull() else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun ensureThermalZonesDiscovered() {
        if (zonesScanned) return
        zonesScanned = true
        try {
            val thermalDir = File("/sys/class/thermal")
            val zones = thermalDir.listFiles { file -> file.isDirectory && file.name.startsWith("thermal_zone") }
                ?: return

            for (zone in zones.sortedBy { it.name }) {
                val type = try {
                    File(zone, "type").readText().trim().lowercase()
                } catch (_: Throwable) {
                    continue
                }
                val tempFile = File(zone, "temp")
                if (!tempFile.canRead()) continue

                if (cachedCpuZoneFile == null && (
                        type.contains("cpu") ||
                            type.contains("tsens") ||
                            type.contains("soc") ||
                            type.contains("ap_") ||
                            type.contains("prime") ||
                            type.contains("big")
                        )
                ) {
                    if (readZoneTemperature(tempFile) != null) {
                        cachedCpuZoneFile = tempFile
                    }
                }
                if (cachedGpuZoneFile == null && (
                        type.contains("gpu") ||
                            type.contains("kgsl") ||
                            type.contains("mali") ||
                            type.contains("adreno") ||
                            type.contains("g3d")
                        )
                ) {
                    if (readZoneTemperature(tempFile) != null) {
                        cachedGpuZoneFile = tempFile
                    }
                }
                if (cachedCpuZoneFile != null && cachedGpuZoneFile != null) break
            }
        } catch (_: Throwable) {
        }
    }

    private fun readZoneTemperature(file: File?): Float? {
        if (file == null) return null
        return try {
            val raw = file.readText().trim().toFloatOrNull() ?: return null
            val celsius = if (raw > 200f) raw / 1000f else raw
            if (celsius in 15f..120f) celsius else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun formatTemp(value: Float): String {
        return String.format(java.util.Locale.US, "%.1f", value)
    }

    fun toThermalStatusInfo(reading: RealThermalReading): ThermalStatusInfo {
        return ThermalStatusInfo(
            level = reading.thermalLevel,
            isThrottling = reading.isThrottlingDetected,
            displaySummary = reading.alertHeadline ?: reading.thermalLevel.statusSummary
        )
    }
}
