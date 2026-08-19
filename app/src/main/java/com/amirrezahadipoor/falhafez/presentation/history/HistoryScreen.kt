package com.amirrezahadipoor.falhafez.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.core.util.Jalali
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.presentation.components.EmptyState
import com.amirrezahadipoor.falhafez.presentation.components.PoemDetail
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = hiltViewModel()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favorite.isSelectedFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val selectedDraw = selectedId?.let { id -> history.firstOrNull { it.id == id } }

    if (selectedDraw != null) {
        RitualBackground(spec = spec, showParticles = false) {
            PoemDetail(
                poem = selectedDraw.poem,
                category = selectedDraw.category,
                spec = spec,
                isFavorite = isFavorite,
                onToggleFavorite = viewModel.favorite::toggleSelected,
                onBack = viewModel::close
            )
        }
        return
    }

    Box(Modifier.fillMaxSize().background(FalPalette.Navy)) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "تاریخچه")
            if (history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.History,
                        title = "هنوز فالی نگرفته‌اید",
                        subtitle = "فال‌هایی که می‌گیرید اینجا ثبت می‌شوند."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history.size) { index ->
                        HistoryItem(draw = history[index], onClick = { viewModel.open(history[index].id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(draw: DrawEntry, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = Jalali.format(draw.drawnAt),
            style = FalText.caption,
            color = FalPalette.Gold
        )
        if (!draw.question.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "نیّت: ${draw.question}",
                style = FalText.caption,
                color = FalPalette.CreamMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = draw.poem.opening,
            style = FalText.verseSmall,
            color = FalPalette.Cream,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
