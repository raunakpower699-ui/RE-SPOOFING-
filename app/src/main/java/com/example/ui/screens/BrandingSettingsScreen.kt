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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserPreferencesEntity
import com.example.model.DeviceCompatibilityReport
import com.example.ui.components.VivoBrandingLogo
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BrandingSettingsScreen(
    preferences: UserPreferencesEntity,
    compatibility: DeviceCompatibilityReport,
    isSessionActive: Boolean,
    onPickCustomLogo: () -> Unit,
    onResetDefaultLogo: () -> Unit,
    onToggleLowBatteryWarning: (Boolean) -> Unit,
    onToggleBootPreferenceRestore: (Boolean) -> Unit,
    onToggleVivoIqooSimulation: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("branding_settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "RE SPOOFING LOGO & VIVO/iQOO LOCK",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engineered by Raunak Exploits. Manage the RE Spoofing application logo, Vivo & iQOO exclusive hardware lock, boot behavior, and battery awareness safeguards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            com.example.ui.components.RaunakExploitsCreatorBadge()
        }

        // App Logo Integration Point Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
                    .testTag("branding_logo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VivoBrandingLogo(
                            customLogoUri = preferences.customLogoUri,
                            size = 76.dp,
                            isSessionActive = isSessionActive
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RE Spoofing Official Logo",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (preferences.customLogoUri != null) {
                                    "Using custom user-selected logo image across RE Spoofing."
                                } else {
                                    "Using official RE Spoofing circular yellow & red anime emblem (img_re_spoofing_logo)."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPickCustomLogo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_custom_logo_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Image, contentDescription = "Select Logo")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Logo Photo", fontWeight = FontWeight.Bold)
                        }

                        if (preferences.customLogoUri != null) {
                            OutlinedButton(
                                onClick = onResetDefaultLogo,
                                modifier = Modifier.testTag("reset_default_logo_button")
                            ) {
                                Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = "Reset Logo")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Default RE Logo", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Vivo & iQOO Exclusive Device Lock Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TelemetryAmber.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .testTag("vivo_iqoo_lock_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Vivo & iQOO Hardware Lock",
                            tint = TelemetryAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIVO & iQOO EXCLUSIVE HARDWARE LOCK",
                            style = MaterialTheme.typography.labelLarge,
                            color = TelemetryAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (compatibility.isRealVivoOrIqooHardware) {
                            "Genuine Vivo / iQOO device detected (${compatibility.manufacturer} ${compatibility.deviceModel}). Full functionality unlocked."
                        } else {
                            "RE Spoofing is locked to Vivo and iQOO phones only. On non-Vivo/iQOO devices or emulators (${compatibility.manufacturer} ${compatibility.deviceModel}), START is blocked unless Emulator Vivo/iQOO Test Mode is toggled on below."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    if (!compatibility.isRealVivoOrIqooHardware) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Simulate Vivo / iQOO Device (Emulator Test)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable this to test RE Spoofing on a non-Vivo emulator.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = compatibility.isVivoIqooSimulationActive,
                                onCheckedChange = onToggleVivoIqooSimulation,
                                modifier = Modifier.testTag("switch_vivo_iqoo_simulation")
                            )
                        }
                    }
                }
            }
        }

        // Boot & Battery Safeguard Preferences
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
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "System Safeguards",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BOOT & BATTERY SAFEGUARDS",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Battery Warning Prompt",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Warn before activating high-performance mode when battery is critically low (≤ 20%) and discharging.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = preferences.warnOnLowBattery,
                            onCheckedChange = onToggleLowBatteryWarning,
                            modifier = Modifier.testTag("switch_low_battery_warning")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CarbonBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restore Selected Profile After Boot",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Restores only your saved profile preference after reboot. Never automatically activates maximum performance without user interaction.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = preferences.rememberPreferenceOnBoot,
                            onCheckedChange = onToggleBootPreferenceRestore,
                            modifier = Modifier.testTag("switch_boot_restore_preference")
                        )
                    }
                }
            }
        }
    }
}
