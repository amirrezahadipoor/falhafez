package ir.falhafez.tabir.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ir.falhafez.tabir.core.designsystem.FalPalette
import ir.falhafez.tabir.core.designsystem.FalText
import ir.falhafez.tabir.core.sound.Sounds

/**
 * Bespoke gold CTA with a moving shimmer sweep, press-scale spring and an
 * optional breathing inner glow — the "heavy polish" button.
 */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    glow: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "gold-press"
    )
    val transition = rememberInfiniteTransition(label = "gold")
    val shimmerT by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gold-shimmer"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gold-glow"
    )
    val contentAlpha by animateFloatAsState(
        if (enabled && !loading) 1f else 0.55f,
        label = "gold-alpha"
    )

    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = {
                    if (Sounds.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Sounds.tap()
                    onClick()
                }
            )
            .background(
                Brush.horizontalGradient(
                    listOf(FalPalette.GoldDeep, FalPalette.Gold, FalPalette.GoldBright)
                ),
                alpha = contentAlpha
            )
    ) {
        if (glow && enabled && !loading) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FalPalette.GoldBright.copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }
        }
        if (enabled && !loading) {
            Canvas(Modifier.matchParentSize()) {
                val w = size.width
                val x = shimmerT * (w + 360f) - 180f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.32f),
                            Color.Transparent
                        ),
                        start = Offset(x - 90f, 0f),
                        end = Offset(x + 90f, size.height)
                    )
                )
            }
        }
        Box(
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = FalPalette.Navy
                )
            } else {
                Text(text = text, style = FalText.button, color = FalPalette.Navy)
            }
        }
    }
}

/** Soft outlined secondary button with press-scale feedback. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = FalPalette.GoldDeep,
    textColor: Color = FalPalette.Cream
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "ghost-press"
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (Sounds.hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Sounds.tap()
                    onClick()
                }
            )
            .padding(horizontal = 28.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = FalText.button, color = textColor)
    }
}

/** Small ornamental divider: a thin line with a central diamond. */
@Composable
fun OrnamentalDivider(
    color: Color = FalPalette.Gold,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val y = size.height / 2f
        val lineColor = color.copy(alpha = 0.55f)
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width * 0.40f, y),
            strokeWidth = 1.5f
        )
        drawLine(
            color = lineColor,
            start = Offset(size.width * 0.60f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.5f
        )
        val c = Offset(size.width / 2f, y)
        val r = 4.5f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(c.x, c.y - r)
            lineTo(c.x + r, c.y)
            lineTo(c.x, c.y + r)
            lineTo(c.x - r, c.y)
            close()
        }
        drawPath(path = path, color = color)
        drawCircle(color = color.copy(alpha = 0.35f), radius = r * 2.1f, center = c)
    }
}

/** Shared top header with optional back button (RTL-aware). */
@Composable
fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = FalPalette.Cream
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = titleColor
                )
            }
            Spacer(Modifier.size(4.dp))
        }
        Text(
            text = title,
            style = FalText.title,
            color = titleColor,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/** Tasteful empty-state used by lists before they have content. */
@Composable
fun EmptyState(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color = FalPalette.CreamMuted
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.6f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
        Text(text = title, style = FalText.heading, color = color)
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, style = FalText.bodyMuted, color = color.copy(alpha = 0.75f))
    }
}
