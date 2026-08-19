package com.amirrezahadipoor.falhafez.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText

/** Bespoke gold CTA button. */
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val alpha by animateFloatAsState(if (enabled && !loading) 1f else 0.55f, label = "goldbtn")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(FalPalette.GoldDeep, FalPalette.Gold, FalPalette.GoldBright)
                ).let { brush -> brush },
                alpha = alpha
            )
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 40.dp, vertical = 15.dp),
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

/** Soft outlined secondary button. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = FalPalette.GoldDeep,
    textColor: Color = FalPalette.Cream
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Transparent)
            .clickable(onClick = onClick)
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = titleColor
                )
            }
            Spacer(Modifier.size(4.dp))
        }
        Text(text = title, style = FalText.title, color = titleColor)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
        }
        Text(text = title, style = FalText.heading, color = color)
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, style = FalText.bodyMuted, color = color.copy(alpha = 0.75f))
    }
}
