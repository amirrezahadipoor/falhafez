package ir.siliksama.falhafez.presentation.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.presentation.components.EmptyState
import ir.siliksama.falhafez.presentation.components.PoemCard
import ir.siliksama.falhafez.presentation.components.PoemDetail
import ir.siliksama.falhafez.presentation.components.RitualBackground
import ir.siliksama.falhafez.presentation.components.ScreenHeader

@Composable
fun FavoritesScreen() {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val themeId by viewModel.themeId.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val isFavorite by viewModel.favorite.isSelectedFavorite.collectAsStateWithLifecycle()
    val spec = FalThemeSpec.byId(themeId)

    val selectedPoem = selectedId?.let { id -> favorites.firstOrNull { it.id == id } }

    BackHandler(enabled = selectedPoem != null) { viewModel.close() }

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

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(spec.backgroundTop, spec.backgroundBottom)))
    ) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "علاقه‌مندی‌ها", titleColor = spec.onBackground)
            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "هنوز چیزی نشان نکرده‌اید",
                        subtitle = "غزل‌ها و ابیات محبوب‌تان را اینجا نگه می‌دارید.",
                        color = spec.onBackgroundMuted
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favorites) { poem ->
                        PoemCard(poem = poem, spec = spec, onClick = { viewModel.open(poem.id) })
                    }
                }
            }
        }
    }
}
