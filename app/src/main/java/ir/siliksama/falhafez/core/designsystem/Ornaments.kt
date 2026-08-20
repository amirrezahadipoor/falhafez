package ir.siliksama.falhafez.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.R

/** Slowly rotating eight-pointed khatam star. */
@Composable
fun RotatingStar(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    tint: Color = FalPalette.Gold,
    durationMillis: Int = 26_000
) {
    val transition = rememberInfiniteTransition(label = "star")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star-angle"
    )
    Icon(
        painter = painterResource(R.drawable.ic_khatam_star),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = angle }
    )
}

/** Breathing + rotating floral rosette, used as a soft halo behind the main CTA. */
@Composable
fun BreathingRosette(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    tint: Color = FalPalette.Gold
) {
    val transition = rememberInfiniteTransition(label = "rosette")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rosette-angle"
    )
    val scale by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rosette-scale"
    )
    Icon(
        painter = painterResource(R.drawable.ic_rosette),
        contentDescription = null,
        tint = tint.copy(alpha = 0.30f),
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = angle
                scaleX = scale
                scaleY = scale
            }
    )
}

/**
 * Mystical breathing ring — a soft circle that expands and fades, inviting the
 * user to breathe slowly before making their wish (نیّت).
 */
@Composable
fun BreathingRing(
    modifier: Modifier = Modifier,
    ringSize: Dp = 120.dp,
    tint: Color = FalPalette.Gold,
    durationMillis: Int = 3800
) {
    val transition = rememberInfiniteTransition(label = "breath")
    val scale by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath-scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath-alpha"
    )
    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension / 2f * scale
            drawCircle(color = tint.copy(alpha = alpha), radius = radius, center = Offset(cx, cy))
            drawCircle(
                color = tint.copy(alpha = alpha * 0.7f),
                radius = radius,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/** Crescent-moon-and-star icon (mystical night motif). */
@Composable
fun MoonStar(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    tint: Color = FalPalette.Gold
) {
    Icon(
        painter = painterResource(R.drawable.ic_moon_star),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size)
    )
}

/** Boteh (paisley) ornament. */
@Composable
fun Boteh(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = FalPalette.Gold
) {
    Icon(
        painter = painterResource(R.drawable.ic_boteh),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size)
    )
}

/** Tazhib-style corner flourishes overlaid on the four corners of a card. */
@Composable
fun CornerOrnaments(
    modifier: Modifier = Modifier,
    tint: Color = FalPalette.Gold,
    size: Dp = 46.dp
) {
    Box(modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_corner_motif),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size).align(Alignment.TopStart)
        )
        Icon(
            painter = painterResource(R.drawable.ic_corner_motif),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size).align(Alignment.TopEnd).rotate(90f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_corner_motif),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size).align(Alignment.BottomEnd).rotate(180f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_corner_motif),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size).align(Alignment.BottomStart).rotate(270f)
        )
    }
}

/** Ornamental laurel divider (line + mini khatam star + dots). */
@Composable
fun LaurelDivider(
    modifier: Modifier = Modifier,
    tint: Color = FalPalette.Gold,
    height: Dp = 28.dp
) {
    Icon(
        painter = painterResource(R.drawable.ic_laurel_divider),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}
