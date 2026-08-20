package com.amirrezahadipoor.falhafez.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.BreathingRing
import com.amirrezahadipoor.falhafez.core.designsystem.BreathingRosette
import com.amirrezahadipoor.falhafez.core.designsystem.CornerOrnaments
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.designsystem.LaurelDivider
import com.amirrezahadipoor.falhafez.core.designsystem.MoonStar
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.Jalali
import com.amirrezahadipoor.falhafez.core.util.PersianText
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.presentation.ads.BannerAdView
import com.amirrezahadipoor.falhafez.presentation.components.GhostButton
import com.amirrezahadipoor.falhafez.presentation.components.GoldButton
import com.amirrezahadipoor.falhafez.presentation.components.OrnamentalDivider
import com.amirrezahadipoor.falhafez.presentation.share.SharePoemButton
import kotlinx.coroutines.delay

/* ------------------------------------------------------------------ */
/*  Category-specific interpretation angles (secondary reading beat)   */
/* ------------------------------------------------------------------ */
object CategoryAngles {
    private val map = mapOf(
        FalCategory.LOVE to "نگاهِ این فال به «عشق»: اگر نیّتت دل است، پیامِ شعر آن است که مهرِ واقعی با صبوری و بخشندگی می‌ماند؛ دلت را ساده بگیر و از ابرازِ آن نترس.",
        FalCategory.CAREER to "نگاهِ این فال به «کار و پیشه»: آنچه در پیِ آنی با کوششِ آرام و پرهیز از شتاب به دست می‌آید؛ راهِ پیشِ رو باز است، فقط قدمِ بعدی را بردار.",
        FalCategory.TRAVEL to "نگاهِ این فال به «سفر»: جابه‌جایی در زندگی‌ات در پیش است؛ مقصد همان‌جاست که دلت آن را می‌شناسد، و راه، خودش بخشی از پاسخ است.",
        FalCategory.HEALTH to "نگاهِ این فال به «سلامتی»: آرامشِ دل بزرگ‌ترین داروست؛ اندوه را سبک کن و به روندِ بهبود اعتماد کن؛ نیروی شفا در خودِ توست.",
        FalCategory.DECISION to "نگاهِ این فال به «تصمیم»: دلِ تو از پیش انتخابش را کرده است؛ آنچه می‌ماند شجاعتِ اعلامِ آن است؛ با آرامش قدم بردار."
    )

    fun text(category: FalCategory): String? = map[category]
}

/* ------------------------------------------------------------------ */
/*  Niyyat — a single, scroll-free ritual screen                       */
/* ------------------------------------------------------------------ */
@Composable
fun NiyyatContent(
    spec: FalThemeSpec,
    state: HomeUiState,
    onQuestionChange: (String) -> Unit,
    onCategorySelect: (FalCategory) -> Unit,
    onDraw: () -> Unit,
    onRewardedDraw: (() -> Unit)? = null,
    onDailyFal: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---- fixed header ----
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "هم‌فال", style = FalText.displaySmall, color = spec.accentSoft)
                Text(text = "دیوان و فالِ حافظ", style = FalText.caption, color = spec.onBackgroundMuted)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "تنظیمات", tint = spec.onBackgroundMuted)
            }
        }
        OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.6f))

        // ---- centered ritual (fits, no scroll) ----
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                BreathingRing(tint = spec.accent, ringSize = 92.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("دل به نیّت بسپار", style = FalText.heading, color = spec.onBackground)
                    Text("نفس بکش…", style = FalText.caption, color = spec.onBackgroundMuted)
                }
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = state.question,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("نیّت خود را بنویس… (اختیاری)", style = FalText.caption) },
                textStyle = FalText.body,
                minLines = 1,
                maxLines = 2,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = spec.accent,
                    unfocusedBorderColor = spec.border.copy(alpha = 0.6f),
                    focusedTextColor = spec.onBackground,
                    unfocusedTextColor = spec.onBackground,
                    cursorColor = spec.accent,
                    focusedContainerColor = spec.card.copy(alpha = 0.6f),
                    unfocusedContainerColor = spec.card.copy(alpha = 0.4f),
                    focusedPlaceholderColor = spec.onBackgroundMuted,
                    unfocusedPlaceholderColor = spec.onBackgroundMuted
                )
            )

            Spacer(Modifier.height(10.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                items(FalCategory.entries.filter { it != FalCategory.NONE }) { category ->
                    val selected = category == state.category
                    FilterChip(
                        selected = selected,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category.faName, style = FalText.caption) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = spec.accent,
                            selectedLabelColor = Color(0xFF14100A),
                            containerColor = spec.card.copy(alpha = 0.5f),
                            labelColor = spec.onBackgroundMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = spec.border.copy(alpha = 0.5f),
                            selectedBorderColor = spec.accentSoft
                        )
                    )
                }
            }
        }

        // ---- fixed bottom: CTA + daily fal + banner ----
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BreathingRosette(tint = spec.accent, size = 130.dp)
            if (state.remainingToday <= 0 && onRewardedDraw != null) {
                GoldButton(text = "فال بیشتر — تماشای ویدئو", onClick = onRewardedDraw, glow = true)
            } else {
                GoldButton(
                    text = when {
                        state.busy -> "در حال گشودن دیوان…"
                        state.remainingToday <= 0 -> "فالِ رایگانِ امروز تمام شد"
                        else -> "فال بگیر"
                    },
                    onClick = onDraw,
                    enabled = state.canDraw,
                    glow = true
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(spec.card.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .border(1.dp, spec.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable(onClick = onDailyFal)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoonStar(tint = spec.accent, size = 26.dp)
            Spacer(Modifier.size(8.dp))
            Text("فالِ امروز", style = FalText.body, color = spec.onBackground, modifier = Modifier.weight(1f))
            Text(Jalali.shortDate(System.currentTimeMillis()), style = FalText.caption, color = spec.onBackgroundMuted)
        }

        if (state.remainingToday in 1..2) {
            Text(
                "فالِ رایگانِ باقی‌ماندهٔ امروز: ${PersianText.number(state.remainingToday)}",
                style = FalText.caption,
                color = spec.onBackgroundMuted
            )
        }

        Spacer(Modifier.height(8.dp))
        BannerAdView()
    }
}

/* ------------------------------------------------------------------ */
/*  Reveal — verse hero with a pinned action button                    */
/* ------------------------------------------------------------------ */
@Composable
fun RevealContent(
    spec: FalThemeSpec,
    poem: Poem,
    onReadInterpretation: () -> Unit
) {
    var visibleCount by remember(poem.id) { mutableIntStateOf(0) }
    var allRevealed by remember(poem.id) { mutableIntStateOf(0) }

    LaunchedEffect(poem.id) {
        for (i in 1..poem.verses.size) {
            visibleCount = i
            delay(430L)
        }
        allRevealed = 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "${poem.collection.poet.faName} — ${poem.collection.faName}",
            style = FalText.caption,
            color = spec.onBackgroundMuted,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))

        // scrollable middle (only if the ghazal is longer than the screen)
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
                    enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { it / 3 }
                ) {
                    VerseView(verse = verse, color = spec.onBackground)
                }
                Spacer(Modifier.height(14.dp))
            }
        }

        // fixed bottom action
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(visible = allRevealed > 0, enter = fadeIn(tween(600))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LaurelDivider(tint = spec.accent, modifier = Modifier.fillMaxWidth(0.7f))
                    Spacer(Modifier.height(12.dp))
                    GoldButton(text = "تفسیرِ فال را بخوان", onClick = onReadInterpretation, glow = true)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Interpretation — tafsir on top, actions pinned at the bottom       */
/* ------------------------------------------------------------------ */
@Composable
fun InterpretationContent(
    spec: FalThemeSpec,
    poem: Poem,
    category: FalCategory,
    isFavorite: Boolean,
    cooldownActive: Boolean,
    remainingToday: Int,
    onToggleFavorite: () -> Unit,
    onDrawAgain: () -> Unit,
    onRewarded: (() -> Unit)? = null,
    onOpenPoem: () -> Unit,
    onDismiss: () -> Unit
) {
    val categoryAngle = CategoryAngles.text(category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("تفسیرِ فال", style = FalText.heading, color = spec.accent)
            Text("گویی دانایی، فالِ تو را می‌خواند…", style = FalText.caption, color = spec.onBackgroundMuted)
        }
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(spec.card.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                    .border(1.dp, spec.border.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            ) {
                CornerOrnaments(modifier = Modifier.matchParentSize(), tint = spec.accent, size = 40.dp)
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = poem.tafsir, style = FalText.tafsir, color = spec.onBackground)
                    if (categoryAngle != null) {
                        Spacer(Modifier.height(12.dp))
                        OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.5f))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = categoryAngle,
                            style = FalText.bodyMuted,
                            color = spec.accentSoft,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
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
            SharePoemButton(poem = poem, category = category, spec = spec, tint = spec.onBackgroundMuted)
            IconButton(onClick = onOpenPoem) {
                Icon(Icons.Outlined.MenuBook, contentDescription = "مشاهده در دیوان", tint = spec.onBackgroundMuted)
            }
        }

        when {
            cooldownActive && onRewarded != null -> GoldButton(
                text = "فال فوری — تماشای ویدئو", onClick = onRewarded, glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            cooldownActive -> GoldButton(
                text = "لحظه‌ای درنگ…", onClick = onDrawAgain, enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            remainingToday > 0 -> GoldButton(
                text = "فال دوباره", onClick = onDrawAgain, glow = true,
                modifier = Modifier.fillMaxWidth()
            )
            else -> GhostButton(
                text = "فالِ رایگانِ امروز تمام شد", onClick = {}, textColor = spec.onBackgroundMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(6.dp))
        GhostButton(text = "بازگشت", onClick = onDismiss, textColor = spec.onBackgroundMuted, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun VerseView(verse: Verse, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = verse.first, style = FalText.verse, color = color)
        if (verse.isCouplet) {
            Spacer(Modifier.height(3.dp))
            Text(text = verse.second!!, style = FalText.verse, color = color)
        }
    }
}
