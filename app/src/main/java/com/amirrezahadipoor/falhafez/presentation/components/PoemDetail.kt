package com.amirrezahadipoor.falhafez.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.Clipboard
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.presentation.share.SharePoemButton

/** Full-poem reader with beit-by-beit meaning, a study mode and copy-to-clipboard. */
@Composable
fun PoemDetail(
    poem: Poem,
    category: FalCategory,
    spec: FalThemeSpec,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showMeaning by remember(poem.id) { mutableStateOf(true) }
    var studyMode by remember(poem.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            title = if (studyMode) "" else "${poem.collection.poet.faName} — ${poem.collection.faName}",
            onBack = onBack,
            titleColor = spec.onBackground
        )

        if (!studyMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = showMeaning,
                    onClick = { showMeaning = !showMeaning },
                    label = { Text("معنی بیت", style = FalText.caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = spec.accent,
                        selectedLabelColor = androidx.compose.ui.graphics.Color(0xFF14100A),
                        containerColor = spec.card.copy(alpha = 0.5f),
                        labelColor = spec.onBackgroundMuted
                    )
                )
                FilterChip(
                    selected = studyMode,
                    onClick = { studyMode = !studyMode },
                    label = { Text("حالت مطالعه", style = FalText.caption) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.height(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = spec.accent,
                        selectedLabelColor = androidx.compose.ui.graphics.Color(0xFF14100A),
                        containerColor = spec.card.copy(alpha = 0.5f),
                        labelColor = spec.onBackgroundMuted
                    )
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        poem.verses.forEach { verse ->
            VerseView(
                verse = verse,
                color = spec.onBackground,
                meaningColor = spec.onBackgroundMuted,
                showMeaning = showMeaning
            )
            Spacer(Modifier.height(16.dp))
        }

        if (!studyMode) {
            OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.7f))
            Spacer(Modifier.height(18.dp))

            Text("تفسیر", style = FalText.heading, color = spec.accent)
            Spacer(Modifier.height(10.dp))
            Text(poem.tafsir, style = FalText.tafsir, color = spec.onBackground)

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "علاقه‌مندی",
                        tint = if (isFavorite) spec.accent else spec.onBackgroundMuted
                    )
                }
                SharePoemButton(
                    poem = poem,
                    category = category,
                    spec = spec,
                    tint = spec.onBackgroundMuted
                )
                IconButton(onClick = { Clipboard.copy(context, "متن شعر", poem.verses.joinToString("\n") { it.fullText } + "\n\n" + poem.tafsir) }) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "کپی متن",
                        tint = spec.onBackgroundMuted
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        } else {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun VerseView(
    verse: Verse,
    color: androidx.compose.ui.graphics.Color,
    meaningColor: androidx.compose.ui.graphics.Color,
    showMeaning: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(verse.first, style = FalText.verse, color = color)
        if (verse.isCouplet) {
            Spacer(Modifier.height(4.dp))
            Text(verse.second!!, style = FalText.verse, color = color)
        }
        if (showMeaning && !verse.meaning.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = verse.meaning,
                style = FalText.caption,
                color = meaningColor,
                modifier = Modifier
                    .background(
                        meaningColor.copy(alpha = 0.08f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
