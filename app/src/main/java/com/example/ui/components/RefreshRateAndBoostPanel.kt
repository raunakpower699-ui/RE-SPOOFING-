package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceTelemetryState
import com.example.model.RefreshRateTarget
import com.example.model.UniversalDeviceTier
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LiquidGlassGradient
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryGreenGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RefreshRateLockControlCard(
    state: PerformanceTelemetryState,
    onSelectRefreshRate: (RefreshRateTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val perms = state.refreshRatePermissions

    val accentColor = if (state.isSessionActive) DiabloRed else ElectricCyan
    val glowColor = if (state.isSessionActive) DiabloRedGlow else ElectricCyanGlow

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                listOf(
                    accentColor.copy(alpha = 0.7f),
                    GlassBorder,
                    TelemetryGreen.copy(alpha = 0.35f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(22.dp), ambientColor = glowColor, spotColor = glowColor)
            .testTag("refresh_rate_lock_card")
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(16.dp)
        ) {
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
                        imageVector = Icons.Filled.DisplaySettings,
                        contentDescription = "Refresh Rate Lock",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REFRESH RATE LOCK",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TelemetryGreen.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryGreen.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BatteryChargingFull,
                            contentDescription = "1% Battery Lock",
                            tint = TelemetryGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "1% BATTERY SAFE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetryGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select 90 / 120 / 144 / 165 Hz • OriginOS 6/7 Android 16 Fixed Lock (Even at 1% Battery)",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RefreshRateTarget.entries.forEach { target ->
                    val isSelected = state.selectedRefreshRate == target
                    RefreshRateTile(
                        target = target,
                        isSelected = isSelected,
                        isLockedActive = state.isSessionActive && isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectRefreshRate(target)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hardware panel info + active lock banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GlassSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.isSessionActive) {
                                "PINNED AT ${state.selectedRefreshRate.label} • BATTERY ${state.batteryStatus.levelPercent}% BYPASSED"
                            } else {
                                "SELECTED: ${state.selectedRefreshRate.label} • PANEL MAX ${state.deviceSpecs.panelMaxRefreshRateHz}Hz"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isSessionActive) TelemetryGreen else TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Supported Panel Modes: ${state.deviceSpecs.supportedRefreshRatesHz.joinToString(" / ") { "${it}Hz" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = if (state.isSessionActive) TelemetryGreen else ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OriginOS 6/7 Android 16 System Permission Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PermissionStatusChip(
                    label = "Overlay Anchor",
                    granted = perms.canDrawOverlays,
                    onClick = { openOverlayPermission(context) },
                    modifier = Modifier.weight(1f)
                )
                PermissionStatusChip(
                    label = "System Hz Write",
                    granted = perms.canWriteSystemSettings,
                    onClick = { openWriteSettingsPermission(context) },
                    modifier = Modifier.weight(1f)
                )
                PermissionStatusChip(
                    label = "1% Battery Exempt",
                    granted = perms.isBatteryOptimizationIgnored,
                    onClick = { openBatteryOptimizationExemption(context) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (!perms.allPermissionsReady) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DiabloOrange.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DiabloOrange.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable {
                            when {
                                !perms.canDrawOverlays -> openOverlayPermission(context)
                                !perms.canWriteSystemSettings -> openWriteSettingsPermission(context)
                                !perms.isBatteryOptimizationIgnored -> openBatteryOptimizationExemption(context)
                            }
                        }
                        .testTag("grant_originos_permissions_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Unlock Permissions",
                            tint = DiabloOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TAP TO UNLOCK GLOBAL HZ & 1% BATTERY BYPASS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshRateTile(
    target: RefreshRateTarget,
    isSelected: Boolean,
    isLockedActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "hzTileScale"
    )

    val tileColor = when {
        isLockedActive -> DiabloRed
        isSelected -> ElectricCyan
        else -> TextSecondary
    }
    val tileGlow = when {
        isLockedActive -> DiabloRedGlow
        isSelected -> ElectricCyanGlow
        else -> Color.Transparent
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CobaltDeep else GlassSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) tileColor else GlassBorder
        ),
        modifier = modifier
            .heightIn(min = 68.dp)
            .scale(scale)
            .then(
                if (isSelected) Modifier.shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = tileGlow, spotColor = tileGlow)
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("hz_button_${target.hz}")
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.verticalGradient(
                            listOf(tileColor.copy(alpha = 0.28f), CobaltDeep)
                        )
                    } else {
                        LiquidGlassGradient
                    }
                )
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${target.hz}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Hz • ${target.badge}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) tileColor else TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PermissionStatusChip(
    label: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = if (granted) TelemetryGreen else DiabloOrange
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = chipColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, chipColor.copy(alpha = 0.55f)),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                contentDescription = label,
                tint = chipColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UniversalMakkhanTunerCard(
    state: PerformanceTelemetryState,
    onSelectUniversalTier: (UniversalDeviceTier) -> Unit,
    onCleanAndOptimizeRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("universal_tuner_card")
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Memory,
                        contentDescription = "RAM & Engine",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "UNIVERSAL ENGINE • ${state.deviceSpecs.totalRamGb}GB RAM",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${state.deviceSpecs.availableRamGb}GB Free • ${state.deviceSpecs.cpuCoreConfiguration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TelemetryGreen.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryGreen),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp), ambientColor = TelemetryGreenGlow, spotColor = TelemetryGreenGlow)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCleanAndOptimizeRam()
                        }
                        .testTag("ram_clean_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = "Clean RAM",
                            tint = TelemetryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.freedRamMbLastClean > 0) "FREED ${state.freedRamMbLastClean}MB" else "BOOST RAM",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetryGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UniversalDeviceTier.entries.forEach { tier ->
                    val isSelected = state.activeUniversalTier == tier
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CobaltDeep else GlassSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (isSelected) ElectricCyan else GlassBorder
                        ),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectUniversalTier(tier)
                            }
                            .testTag("tier_chip_${tier.id}")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tier.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openOverlayPermission(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Throwable) {
    }
}

private fun openWriteSettingsPermission(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Throwable) {
    }
}

private fun openBatteryOptimizationExemption(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Throwable) {
        try {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        } catch (_: Throwable) {
        }
    }
}
