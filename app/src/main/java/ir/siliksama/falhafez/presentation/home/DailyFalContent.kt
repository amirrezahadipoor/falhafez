package ir.siliksama.falhafez.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.CornerOrnaments
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.designsystem.MoonStar
import ir.siliksama.falhafez.core.designsystem.RotatingStar
import ir.siliksama.falhafez.core.designsystem.readingColor
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.Jalali
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.presentation.components.GhostButton
import ir.siliksama.falhafez.presentation.share.SharePoemButton
import kotlinx.coroutines.delay

/** فالِ امروز — deterministic daily fal, scroll only where the ghazal needs it. */
@Composable
fun DailyFalContent(
    spec: FalThemeSpec,
    poem: Poem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    var visibleCount by remember(poem.id) { mutableIntStateOf(0) }

    LaunchedEffect(poem.id) {
        for (i in 1..poem.verses.size) {
            visibleCount = i
            delay(380L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        // fixed header
        Box(contentAlignment = Alignment.Center) {
            RotatingStar(tint = spec.accent.copy(alpha = 0.4f), size = 92.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("فالِ امروز", style = FalText.heading, color = spec.accentSoft)
                Text(Jalali.shortDate(System.currentTimeMillis()), style = FalText.caption, color = spec.onBackgroundMuted)
            }
        }
        Text(
            "این فال، امروز برای همه یکی است؛ شاید فالِ امروزِ من و تو یکی باشد.",
            style = FalText.caption,
            color = spec.onBackgroundMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        MoonStar(tint = spec.accent, size = 30.dp, modifier = Modifier.align(Alignment.CenterHorizontally))

        // scrollable middle
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            poem.verses.forEachIndexed { index, verse ->
                AnimatedVisibility(
                    visible = index < visibleCount,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(verse.first, style = FalText.verse, color = readingColor(spec.onBackground))
                        if (verse.isCouplet) {
                            Spacer(Modifier.height(3.dp))
                            Text(verse.second!!, style = FalText.verse, color = readingColor(spec.onBackground))
                        }
                    }
                }
                Spacer(Modifier.height(13.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(spec.card.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                    .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                CornerOrnaments(modifier = Modifier.matchParentSize(), tint = spec.accent, size = 38.dp)
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تفسیر", style = FalText.heading, color = spec.accent)
                    Spacer(Modifier.height(8.dp))
                    Text(poem.tafsir, style = FalText.tafsir, color = readingColor(spec.onBackground))
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // fixed bottom actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "علاقه‌مندی",
                    tint = if (isFavorite) spec.accent else spec.onBackgroundMuted
                )
            }
            SharePoemButton(poem = poem, category = FalCategory.NONE, spec = spec, tint = spec.onBackgroundMuted)
        }
        GhostButton(text = "بازگشت", onClick = onBack, textColor = spec.onBackgroundMuted, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
    }
}
