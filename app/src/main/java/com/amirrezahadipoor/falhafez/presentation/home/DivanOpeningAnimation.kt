package com.amirrezahadipoor.falhafez.presentation.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import androidx.compose.material3.Text
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The single most important animation in the app: the closed Divan opens —
 * covers swing outward, pages flutter and settle, golden dust converges and
 * bursts into the "chosen page". Runs ~3s then calls [onFinished].
 */
@Composable
fun DivanOpeningAnimation(
    spec: FalThemeSpec,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val p = progress.value
        Canvas(Modifier.fillMaxSize()) {
            drawGlow(p, spec)
            drawPages(p, spec)
            drawCovers(p, spec)
            drawParticles(p, spec)
        }
        Text(
            text = "گشودن دیوان…",
            style = FalText.bodyMuted,
            color = spec.onBackgroundMuted.copy(alpha = (1f - p).coerceIn(0f, 1f)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp)
        )
    }
}

private fun DrawScope.drawGlow(p: Float, spec: FalThemeSpec) {
    val cx = size.width / 2f
    val cy = size.height * 0.46f
    val settle = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
    val radius = size.minDimension * (0.34f + 0.3f * settle)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(spec.accent.copy(alpha = 0.08f + 0.30f * settle), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawPages(p: Float, spec: FalThemeSpec) {
    val cx = size.width / 2f
    val cy = size.height * 0.46f
    val rise = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)

    // flutter pages underneath the covers while opening
    val open = ((p - 0.35f) / 0.45f).coerceIn(0f, 1f)
    val coverW = size.width * 0.30f
    val coverH = size.height * 0.40f
    for (i in 0 until 5) {
        val t = i / 5f
        val alpha = (0.35f * (1f - t)) * open * (1f - rise)
        if (alpha <= 0f) continue
        val flutter = sin((p * 7f + t * 2f) * PI).toFloat() * 10f * open
        val pageW = coverW * 1.7f
        val pageH = coverH * 0.82f
        drawRoundRect(
            color = spec.card.copy(alpha = alpha),
            topLeft = Offset(cx - pageW / 2f + flutter, cy - pageH / 2f - t * 12f),
            size = Size(pageW, pageH),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
    }

    // the "chosen page" rises and settles
    val pageAlpha = rise
    if (pageAlpha > 0f) {
        val pageW = coverW * 1.55f
        val pageH = coverH * 0.86f
        val pageTop = cy - pageH * (0.5f + 0.35f * rise)
        drawRoundRect(
            color = spec.card.copy(alpha = pageAlpha * 0.92f),
            topLeft = Offset(cx - pageW / 2f, pageTop),
            size = Size(pageW, pageH),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
        drawRoundRect(
            color = spec.accent.copy(alpha = pageAlpha * 0.85f),
            topLeft = Offset(cx - pageW / 2f, pageTop),
            size = Size(pageW, pageH),
            cornerRadius = CornerRadius(10.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
        // a soft vertical rule on the page, like a calligraphy column
        drawLine(
            color = spec.accent.copy(alpha = pageAlpha * 0.5f),
            start = Offset(cx, pageTop + pageH * 0.16f),
            end = Offset(cx, pageTop + pageH * 0.84f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawCovers(p: Float, spec: FalThemeSpec) {
    val cx = size.width / 2f
    val cy = size.height * 0.46f
    val coverW = size.width * 0.30f
    val coverH = size.height * 0.40f
    val breathe = 1f + 0.015f * sin(p * 9f * PI).toFloat()
    val open = ((p - 0.35f) / 0.45f).coerceIn(0f, 1f)
    val angle = 72f * open

    // spine highlight while closed
    val spineAlpha = (1f - open).coerceIn(0f, 1f)
    drawLine(
        color = spec.accent.copy(alpha = spineAlpha * 0.9f),
        start = Offset(cx, cy - coverH / 2f),
        end = Offset(cx, cy + coverH / 2f),
        strokeWidth = 2.dp.toPx()
    )

    rotate(degrees = -angle, pivot = Offset(cx, cy)) {
        drawCover(Offset(cx - coverW, cy - coverH / 2f), Size(coverW, coverH), spec, breathe)
    }
    rotate(degrees = angle, pivot = Offset(cx, cy)) {
        drawCover(Offset(cx, cy - coverH / 2f), Size(coverW, coverH), spec, breathe)
    }
}

private fun DrawScope.drawCover(topLeft: Offset, size: Size, spec: FalThemeSpec, breathe: Float) {
    val w = size.width * breathe
    val x = topLeft.x + (size.width - w) / 2f
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(spec.backgroundTop.copy(alpha = 0.95f), spec.backgroundBottom)
        ),
        topLeft = Offset(x, topLeft.y),
        size = Size(w, size.height),
        cornerRadius = CornerRadius(8.dp.toPx())
    )
    drawRoundRect(
        color = spec.accent.copy(alpha = 0.9f),
        topLeft = Offset(x, topLeft.y),
        size = Size(w, size.height),
        cornerRadius = CornerRadius(8.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
    val emX = x + w / 2f
    val emY = topLeft.y + size.height / 2f
    val r = 9.dp.toPx()
    val diamond = Path().apply {
        moveTo(emX, emY - r)
        lineTo(emX + r, emY)
        lineTo(emX, emY + r)
        lineTo(emX - r, emY)
        close()
    }
    drawPath(diamond, spec.accent.copy(alpha = 0.95f))
}

private fun DrawScope.drawParticles(p: Float, spec: FalThemeSpec) {
    val cx = size.width / 2f
    val cy = size.height * 0.46f
    val open = ((p - 0.35f) / 0.45f).coerceIn(0f, 1f)
    val count = 26
    for (i in 0 until count) {
        val angle = (i * 137.5f % 360f).toRadians()
        val baseDist = size.minDimension * (0.13f + (i % 5) * 0.035f)
        val dist = baseDist * (1f - open) + 5.dp.toPx() * open
        val px = cx + cos(angle) * dist
        val py = cy + sin(angle) * dist
        val alpha = (0.12f + 0.55f * (1f - open)).coerceIn(0f, 1f)
        drawCircle(
            color = if (i % 4 == 0) spec.particleSecondary else spec.particle,
            radius = (1.1f + (i % 3) * 0.8f).dp.toPx(),
            center = Offset(px, py),
            alpha = alpha
        )
    }
}

private fun Float.toRadians(): Float = this * (PI.toFloat() / 180f)
