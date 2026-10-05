package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeviceCompatibilityReport
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Displays Device Compatibility, Vivo/iQOO Compatibility, Graceful Error Handling,
 * and Important Technical Limitations (Sections 9, 10, 11, 15, 16, 20).
 */
@Composable
fun CompatibilityScreen(
    compatibility: DeviceCompatibilityReport,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("compatibility_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HARDWARE & API COMPATIBILITY",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time capability matrix inspecting official Android and Vivo/iQOO interfaces. Unsupported APIs degrade gracefully without crashing or fabricating telemetry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Device Hardware Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = "Device Hardware",
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DETECTED DEVICE ENVIRONMENT",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    CompatibilityInfoLine("Manufacturer / Brand", "${compatibility.manufacturer} (${compatibility.brand})")
                    CompatibilityInfoLine("Model / SoC", "${compatibility.deviceModel} • ${compatibility.socOrHardware}")
                    CompatibilityInfoLine("Android Version", "Android ${compatibility.androidRelease} (API ${compatibility.sdkInt})")
                    CompatibilityInfoLine("Vivo / iQOO Family", if (compatibility.isVivoOrIqoo) "Yes (${compatibility.vivoOsInfo})" else "Standard Android OEM")
                }
            }
        }

        // Section 15: Explicit Feature Support Matrix
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .testTag("api_support_matrix_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ANDROID PERFORMANCE API MATRIX",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )

                    ApiFeatureRow(
                        featureName = "CPU Performance Hint",
                        statusText = if (compatibility.cpuPerformanceHintSupported) "Supported" else "Fallback (Standard OS Priority)",
                        isSupported = compatibility.cpuPerformanceHintSupported,
                        explanation = if (compatibility.cpuPerformanceHintSupported) {
                            "Android PerformanceHintManager (API 31+) is available and accepts target work duration sessions."
                        } else {
                            "PerformanceHintManager session rate not exposed by device HAL; using standard thread scheduling hints."
                        }
                    )

                    HorizontalDivider(color = CarbonBorder)

                    ApiFeatureRow(
                        featureName = "GPU Performance Hint",
                        statusText = if (compatibility.gpuPerformanceHintExposed) "Supported" else "Not exposed by this device",
                        isSupported = compatibility.gpuPerformanceHintExposed,
                        explanation = stringResource(id = R.string.gpu_fallback_notice)
                    )

                    HorizontalDivider(color = CarbonBorder)

                    ApiFeatureRow(
                        featureName = "Game Mode",
                        statusText = if (compatibility.gameModeSupported) "Supported (${compatibility.currentGameModeLabel})" else "Not exposed by this device",
                        isSupported = compatibility.gameModeSupported,
                        explanation = "Uses Android GameManager API (API 31+/33+) to query active Game Mode and signal gameplay state to the OS."
                    )

                    HorizontalDivider(color = CarbonBorder)

                    ApiFeatureRow(
                        featureName = "Thermal Monitoring",
                        statusText = if (compatibility.thermalMonitoringSupported) "Supported" else "Battery Temp Fallback",
                        isSupported = compatibility.thermalMonitoringSupported,
                        explanation = "Uses PowerManager Thermal Status Listener & Thermal Headroom APIs while keeping thermal safeguards strictly enabled."
                    )

                    HorizontalDivider(color = CarbonBorder)

                    ApiFeatureRow(
                        featureName = "Sustained Performance API",
                        statusText = if (compatibility.sustainedPerformanceSupported) "Supported" else "Not exposed by OEM HAL",
                        isSupported = compatibility.sustainedPerformanceSupported,
                        explanation = "Uses PowerManager.isSustainedPerformanceModeSupported and Window.setSustainedPerformanceMode."
                    )
                }
            }
        }

        // Section 10: Vivo / iQOO OEM Compatibility & Legitimate Operation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CobaltTurbo.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("vivo_iqoo_compatibility_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Vivo iQOO Compatibility",
                            tint = CobaltTurbo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIVO / iQOO OEM COMPATIBILITY",
                            style = MaterialTheme.typography.labelLarge,
                            color = CobaltTurbo
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.oem_unavailable_notice),
                        style = MaterialTheme.typography.titleMedium,
                        color = TelemetryAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = compatibility.oemPerformanceControlStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Legitimate Architecture: Vivo Spoofing never fakes benchmark package names (e.g., AnTuTu), never copies certificates/signatures, and never exploits privileged system services.\n" +
                            "• Privilege Separation: Direct CPU/GPU frequency governor locking (/sys/devices/system/cpu/cpufreq write access or proprietary Vivo Multi-Turbo Binder interfaces) requires OEM platform signature or root privileges unavailable to standard third-party applications.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section 20: Important Technical Limitation & OS Final Authority
        item {
            Surface(
                color = CarbonSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryGreen.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("os_authority_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "OS Final Authority",
                            tint = TelemetryGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OS AUTHORITY & HARDWARE SAFETY GUARANTEE",
                            style = MaterialTheme.typography.labelLarge,
                            color = TelemetryGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Request maximum available performance within Android/OEM limits.",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vivo Spoofing never claims to guarantee 100% fixed CPU/GPU utilization or disable thermal throttling. The Android operating system and OEM firmware retain final authority over:\n" +
                            "• CPU & GPU frequency scaling\n" +
                            "• Thermal throttling & hardware temperature limits\n" +
                            "• Power delivery limits & battery protection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CompatibilityInfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun ApiFeatureRow(
    featureName: String,
    statusText: String,
    isSupported: Boolean,
    explanation: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isSupported) Icons.Filled.CheckCircle else Icons.Filled.Info,
                    contentDescription = featureName,
                    tint = if (isSupported) TelemetryGreen else TelemetryAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = featureName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusPillBadge(
                text = statusText,
                color = if (isSupported) TelemetryGreen else TelemetryAmber
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
