package ir.falhafez.tabir.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import ir.falhafez.tabir.core.theme.FalThemeSpec
import kotlin.math.PI
import kotlin.math.sin

/**
 * The atmospheric backdrop for the whole ritual: a vertical gradient, the theme's
 * original generated artwork (if any), a soft central glow and gently twinkling
 * particles — all driven by the active [FalThemeSpec].
 */
@Composable
fun RitualBackground(
    spec: FalThemeSpec,
    modifier: Modifier = Modifier,
    showParticles: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val artwork = remember(spec.artworkRes) {
        spec.artworkRes?.let { res ->
            runCatching {
                BitmapFactory.decodeResource(context.resources, res).asImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(spec.backgroundTop, spec.backgroundBottom))
            )
    ) {
        if (artwork != null) {
            Image(
                bitmap = artwork,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = spec.artworkAlpha }
            )
        }
        if (showParticles) {
            ParticlesLayer(spec)
        }
        content()
    }
}

@Composable
private fun ParticlesLayer(spec: FalThemeSpec) {
    val transition = rememberInfiniteTransition(label = "ritual-bg")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ritual-bg-time"
    )
    Canvas(Modifier.fillMaxSize()) {
        drawGlow(spec)
        drawParticles(spec, time)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlow(spec: FalThemeSpec) {
    val center = Offset(size.width / 2f, size.height * 0.36f)
    val radius = size.minDimension * 0.6f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(spec.accent.copy(alpha = 0.10f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawParticles(spec: FalThemeSpec, t: Float) {
    val count = 52
    for (i in 0 until count) {
        val fx = ((i * 73) % 101) / 101f
        val fy = ((i * 37 + 11) % 97) / 97f
        val phase = i * 0.9f
        val twinkle = 0.5f + 0.5f * sin((t * 2f * PI + phase).toFloat())
        val alpha = 0.12f + 0.45f * twinkle
        val radius = (0.6f + (i % 4) * 0.45f) * density
        val color = if (i % 5 == 0) spec.particleSecondary else spec.particle
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = Offset(fx * size.width, fy * size.height)
        )
    }
}
