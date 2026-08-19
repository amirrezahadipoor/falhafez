package com.amirrezahadipoor.falhafez.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.presentation.components.EmptyState
import com.amirrezahadipoor.falhafez.presentation.components.PoemCard
import com.amirrezahadipoor.falhafez.presentation.components.PoemDetail
import com.amirrezahadipoor.falhafez.presentation.components.RitualBackground
import com.amirrezahadipoor.falhafez.presentation.components.ScreenHeader

@Composable
fun FavoritesScreen() {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favorite.isSelectedFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val selectedPoem = selectedId?.let { id -> favorites.firstOrNull { it.id == id } }

    if (selectedPoem != null) {
        RitualBackground(spec = spec, showParticles = false) {
            PoemDetail(
                poem = selectedPoem,
                category = FalCategory.NONE,
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
            ScreenHeader(title = "علاقه‌مندی‌ها")
            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "هنوز چیزی نشان نکرده‌اید",
                        subtitle = "غزل‌ها و ابیات محبوب‌تان را اینجا نگه می‌دارید."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites.size) { index ->
                        PoemCard(poem = favorites[index], onClick = { viewModel.open(favorites[index].id) })
                    }
                }
            }
        }
    }
}
