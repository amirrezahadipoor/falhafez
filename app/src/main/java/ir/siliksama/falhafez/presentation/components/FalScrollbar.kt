package ir.siliksama.falhafez.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.FalPalette
import kotlinx.coroutines.delay

/**
 * یک اسکرول‌بار ظریف و طلایی که هنگام اسکرول ظاهر و بعد محو می‌شود —
 * تا کاربر همیشه بداند محتوای بیشتری هست، بدون آنکه مزاحم باشد.
 */
@Composable
fun FalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    color: Color = FalPalette.Gold,
    trackAlpha: Float = 0.18f
) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && scrollState.value != 0) {
            alpha.snapTo(0.55f)
            delay(850)
            alpha.animateTo(0f, tween(400))
        }
    }
    if (scrollState.maxValue <= 0) return

    Canvas(modifier = modifier) {
        val h = size.height
        val trackH = h
        // track
        drawRoundRect(
            color = color.copy(alpha = trackAlpha * alpha.value.coerceAtLeast(0f)),
            topLeft = Offset(size.width - 4.dp.toPx(), 0f),
            size = Size(4.dp.toPx(), trackH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        // thumb
        val max = scrollState.maxValue.toFloat()
        val viewport = h * (h / (h + max))
        val thumbH = viewport.coerceAtLeast(h * 0.07f)
        val t = (scrollState.value.toFloat() / max).coerceIn(0f, 1f)
        val thumbY = t * (h - thumbH)
        drawRoundRect(
            color = color.copy(alpha = alpha.value.coerceIn(0f, 1f)),
            topLeft = Offset(size.width - 4.dp.toPx(), thumbY),
            size = Size(4.dp.toPx(), thumbH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

/** ستون اسکرول‌شونده با اسکرول‌بار زیبا — پارامترهای Column را می‌پذیرد. */
@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: androidx.compose.ui.Alignment.Horizontal = androidx.compose.ui.Alignment.Start,
    verticalArrangement: androidx.compose.foundation.layout.Arrangement.Vertical = androidx.compose.foundation.layout.Arrangement.Top,
    scrollState: ScrollState = rememberScrollState(),
    scrollbarColor: Color = FalPalette.Gold,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement,
            content = content
        )
        FalScrollbar(
            scrollState = scrollState,
            color = scrollbarColor,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(vertical = 8.dp)
        )
    }
}
