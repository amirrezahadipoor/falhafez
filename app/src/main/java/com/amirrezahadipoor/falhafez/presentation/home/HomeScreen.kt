package com.amirrezahadipoor.falhafez.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.presentation.components.GoldButton
import com.amirrezahadipoor.falhafez.presentation.components.GhostButton
import com.amirrezahadipoor.falhafez.presentation.components.OrnamentalDivider
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground

@Composable
fun HomeScreen(onOpenSettings: () -> Unit) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    RitualBackground(spec = spec) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فال حافظ",
                    style = FalText.displaySmall,
                    color = spec.accentSoft,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "تنظیمات",
                        tint = spec.onBackgroundMuted
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.6f))
            Spacer(Modifier.height(20.dp))

            // Niyyat prompt
            Text(
                text = "دل به نیّت بسپار",
                style = FalText.heading,
                color = spec.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "در دل خود نیتی کن، آرام نفس بکش، و سپس فال بگیر.",
                style = FalText.bodyMuted,
                color = spec.onBackgroundMuted
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.question,
                onValueChange = viewModel::onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("نیّت خود را بنویس… (اختیاری)", style = FalText.bodyMuted)
                },
                textStyle = FalText.body,
                minLines = 2,
                maxLines = 4,
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

            Spacer(Modifier.height(14.dp))
            CategoryChips(selected = state.category, onSelect = viewModel::onCategorySelect, spec = spec)

            Spacer(Modifier.height(24.dp))

            GoldButton(
                text = if (state.drawing) "در حال گشودن دیوان…" else "فال بگیر",
                onClick = viewModel::draw,
                enabled = !state.drawing,
                loading = state.drawing
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = state.lastDraw != null,
                enter = fadeIn() + slideInVertically { it / 6 },
                exit = fadeOut() + slideOutVertically { it / 6 }
            ) {
                state.lastDraw?.let { entry ->
                    RevealCard(
                        poem = entry.poem,
                        category = entry.category,
                        isFavorite = isFavorite,
                        spec = spec,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDrawAgain = viewModel::draw,
                        onDismiss = viewModel::dismissResult
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selected: FalCategory,
    onSelect: (FalCategory) -> Unit,
    spec: FalThemeSpec
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FalCategory.entries.filter { it != FalCategory.NONE }.forEach { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = { Text(category.faName, style = FalText.caption) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = spec.accent,
                    selectedLabelColor = Color(0xFF14100A),
                    containerColor = spec.card.copy(alpha = 0.5f),
                    labelColor = spec.onBackgroundMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = spec.border.copy(alpha = 0.5f),
                    selectedBorderColor = spec.accentSoft
                )
            )
        }
    }
}

@Composable
private fun RevealCard(
    poem: Poem,
    category: FalCategory,
    isFavorite: Boolean,
    spec: FalThemeSpec,
    onToggleFavorite: () -> Unit,
    onDrawAgain: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(spec.card.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
            .border(1.dp, spec.border.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${poem.collection.poet.faName} — ${poem.collection.faName}",
            style = FalText.caption,
            color = spec.onBackgroundMuted
        )
        Spacer(Modifier.height(14.dp))

        poem.verses.forEach { verse ->
            VerseView(verse = verse, color = spec.onBackground)
            Spacer(Modifier.height(16.dp))
        }

        OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.7f))
        Spacer(Modifier.height(16.dp))

        if (category != FalCategory.NONE) {
            Text(
                text = "نگاهِ این فال به «${category.faName}»",
                style = FalText.caption,
                color = spec.accentSoft
            )
            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = "تفسیر",
            style = FalText.heading,
            color = spec.accent
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = poem.tafsir,
            style = FalText.tafsir,
            color = spec.onBackground
        )

        Spacer(Modifier.height(20.dp))

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
            GoldButton(text = "فال دوباره", onClick = onDrawAgain, modifier = Modifier.padding(horizontal = 4.dp))
            GhostButton(text = "بستن", onClick = onDismiss, textColor = spec.onBackgroundMuted)
        }
    }
}

@Composable
private fun VerseView(verse: Verse, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = verse.first, style = FalText.verseSmall, color = color)
        if (verse.isCouplet) {
            Spacer(Modifier.height(6.dp))
            Text(text = verse.second!!, style = FalText.verseSmall, color = color)
        }
    }
}
