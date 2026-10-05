package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BatteryStatusInfo
import com.example.model.DeviceLiveSpecs
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Interactive button with spring bounce animation, glowing border shift, and haptic feedback on click.
 */
@Composable
fun AnimatedGlowButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    baseColor: Color = Color(0xFFFFB300),
    pressedGlowColor: Color = ElectricCyan,
    contentColor: Color = ObsidianBg,
    testTag: String = "",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btn_scale"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed && enabled) pressedGlowColor else baseColor,
        animationSpec = tween(durationMillis = 160),
        label = "btn_color"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isPressed && enabled) Color.White else pressedGlowColor.copy(alpha = 0.65f),
        animationSpec = tween(durationMillis = 160),
        label = "btn_border"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = contentColor,
            disabledContainerColor = baseColor.copy(alpha = 0.22f),
            disabledContentColor = TextPrimary.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isPressed && enabled) 2.dp else 1.dp,
                color = if (enabled) animatedBorderColor else CarbonBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedGlowOutlinedButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    accentColor: Color = TelemetryRed,
    testTag: String = "",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "outlined_btn_scale"
    )

    val animatedBg by animateColorAsState(
        targetValue = if (isPressed && enabled) accentColor.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(150),
        label = "outlined_btn_bg"
    )

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = animatedBg,
            contentColor = accentColor,
            disabledContentColor = TextMuted
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPressed && enabled) 2.dp else 1.5.dp,
            color = if (enabled) accentColor else CarbonBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Displays a clean vector emblem badge for RE Spoofing without any external photo or image asset.
 */
@Composable
fun VivoBrandingLogo(
    customLogoUri: String? = null,
    size: Dp = 56.dp,
    isSessionActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB300), Color(0xFFE50914))
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF140D1E))
            .border(
                width = if (isSessionActive) 2.5.dp else 1.8.dp,
                brush = borderBrush,
                shape = CircleShape
            )
            .testTag("vivo_spoofing_logo"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RE",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFFB300),
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Prominent Creator Badge crediting Raunak Exploits.
 */
@Composable
fun RaunakExploitsCreatorBadge(
    modifier: Modifier = Modifier
) {
    val gradientBorder = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB300), Color(0xFFFF1744), ElectricCyan)
    )
    Surface(
        color = Color(0xFF140D1E),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, gradientBorder, RoundedCornerShape(14.dp))
            .testTag("raunak_exploits_creator_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Created by Raunak Exploits",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CREATED BY RAUNAK EXPLOITS",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official RE Spoofing Engine • Vivo & iQOO Performance Edition",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Displays live phone specifications (Phone Name, Model, Android Version, SoC, RAM, Storage, Display)
 * for any user who opens the app.
 */
@Composable
fun DeviceLiveSpecsCard(
    specs: DeviceLiveSpecs,
    battery: BatteryStatusInfo,
    modifier: Modifier = Modifier
) {
    val cardBorder = Brush.horizontalGradient(
        colors = listOf(ElectricCyan.copy(alpha = 0.6f), CobaltTurbo.copy(alpha = 0.6f))
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, cardBorder, RoundedCornerShape(18.dp))
            .testTag("my_device_live_info_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PhoneAndroid,
                        contentDescription = "Your Phone Info",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "YOUR PHONE LIVE INFORMATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                        Text(
                            text = specs.phoneDisplayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                StatusPillBadge(
                    text = "${specs.displayRefreshRateHz} Hz",
                    color = Color(0xFFFFB300)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CarbonBorder)
            Spacer(modifier = Modifier.height(10.dp))

            SpecInfoRow("Phone Name / Model", "${specs.manufacturer} ${specs.model} (${specs.deviceCodename})")
            SpecInfoRow("Android Version", specs.androidVersionLabel)
            SpecInfoRow("Security Patch / Build", "${specs.securityPatch} • ${specs.buildId}")
            SpecInfoRow("Processor / SoC", "${specs.socProcessor} (${specs.cpuCores} Cores • ${specs.cpuAbi})")
            SpecInfoRow("Display Screen", "${specs.screenResolution} @ ${specs.displayRefreshRateHz}Hz")
            SpecInfoRow("Internal Storage", "${specs.freeStorageGb} / ${specs.totalStorageGb}")
            SpecInfoRow("Battery & Temp", "${battery.levelPercent}% (${battery.powerSourceLabel}) • ${battery.temperatureCelsius}°C")

            Spacer(modifier = Modifier.height(10.dp))

            // Live RAM Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Live RAM Usage (${specs.ramUsedPercent}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${specs.availableRamGb} / ${specs.totalRamGb}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (specs.ramUsedPercent / 100f).coerceIn(0.05f, 1f) },
                color = if (specs.ramUsedPercent > 85) TelemetryAmber else ElectricCyan,
                trackColor = ObsidianBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun SpecInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
fun StatusPillBadge(
    text: String,
    color: Color,
    testTag: String = "",
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f)),
        modifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TelemetryStatusCard(
    title: String,
    subtitleLabel: String,
    stateBadge: String,
    stateColor: Color,
    icon: ImageVector,
    primaryMetric: String,
    secondaryDetail: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = stateColor.copy(alpha = 0.32f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(stateColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = stateColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = subtitleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusPillBadge(text = stateBadge, color = stateColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = primaryMetric,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = secondaryDetail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun VivoIqooExclusiveLockDialog(
    detectedManufacturer: String,
    detectedModel: String,
    onEnableVivoIqooTestMode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurfaceElevated,
        iconContentColor = TelemetryRed,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Vivo & iQOO Exclusive Lock",
                modifier = Modifier.size(34.dp)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.vivo_iqoo_blocked_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(id = R.string.vivo_iqoo_blocked_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Detected Phone: $detectedManufacturer $detectedModel",
                    style = MaterialTheme.typography.labelMedium,
                    color = TelemetryAmber
                )
                Text(
                    text = "Tap 'Unlock Vivo/iQOO Mode' below if you want to unlock and run RE Spoofing on this device or emulator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            AnimatedGlowButton(
                text = "Unlock Vivo/iQOO Mode",
                onClick = onEnableVivoIqooTestMode,
                baseColor = TelemetryAmber,
                pressedGlowColor = ElectricCyan,
                contentColor = ObsidianBg,
                testTag = "enable_vivo_iqoo_emulator_button"
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_vivo_iqoo_lock_button")
            ) {
                Text("Close", color = TextPrimary)
            }
        }
    )
}

@Composable
fun LowBatteryWarningDialog(
    batteryPercent: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonSurfaceElevated,
        iconContentColor = TelemetryAmber,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Filled.BatteryAlert,
                contentDescription = "Low Battery Warning",
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Low Battery Warning ($batteryPercent%)",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.battery_low_warning),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Android battery and thermal protections remain strictly active and will not be overridden. Do you still wish to request Maximum Available Performance Mode?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            AnimatedGlowButton(
                text = "Continue Anyway",
                onClick = onConfirm,
                baseColor = TelemetryAmber,
                pressedGlowColor = ElectricCyan,
                contentColor = ObsidianBg,
                testTag = "confirm_low_battery_start_button"
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_low_battery_start_button")
            ) {
                Text("Cancel", color = TextPrimary)
            }
        }
    )
}
