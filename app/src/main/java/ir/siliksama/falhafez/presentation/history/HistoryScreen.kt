package ir.siliksama.falhafez.presentation.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.Jalali
import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.presentation.components.EmptyState
import ir.siliksama.falhafez.presentation.components.PoemDetail
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.ScreenHeader

@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = hiltViewModel()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favorite.isSelectedFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val showStats by viewModel.showStats.collectAsStateWithLifecycle()
    val selectedDraw = selectedId?.let { id -> history.firstOrNull { it.id == id } }

    // دکمهٔ بازگشت: بستنِ کارنامه یا جزئیات فال
    BackHandler(enabled = showStats || selectedDraw != null) {
        if (showStats) viewModel.toggleStats() else viewModel.close()
    }

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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { ScreenHeader(title = "تاریخچه") }
                IconButton(onClick = viewModel::toggleStats) {
                    Icon(
                        imageVector = Icons.Outlined.Insights,
                        contentDescription = "کارنامهٔ من",
                        tint = if (showStats) FalPalette.GoldBright else FalPalette.CreamMuted
                    )
                }
            }
            if (showStats) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    StatsView(history = history)
                }
                return@Column
            }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(14.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Jalali.shortDate(draw.drawnAt),
            style = FalText.caption,
            color = FalPalette.Gold,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = if (draw.question.isNullOrBlank()) draw.poem.opening else "«${draw.question}» — ${draw.poem.opening}",
            style = FalText.bodyMuted,
            color = FalPalette.Cream,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = FalPalette.CreamMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
