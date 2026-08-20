package ir.falhafez.tabir.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.falhafez.tabir.core.theme.FalThemeId
import ir.falhafez.tabir.domain.model.Poem
import ir.falhafez.tabir.domain.repository.FavoriteRepository
import ir.falhafez.tabir.domain.repository.SettingsRepository
import ir.falhafez.tabir.presentation.components.FavoriteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    favoriteRepository: FavoriteRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val favorites: StateFlow<List<Poem>> = favoriteRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val favorite = FavoriteState(favoriteRepository, viewModelScope)

    private val _selectedId = MutableStateFlow<Long?>(null)
    val selectedId: StateFlow<Long?> = _selectedId.asStateFlow()

    fun open(id: Long) {
        _selectedId.value = id
        favorite.select(id)
    }

    fun close() {
        _selectedId.value = null
        favorite.select(null)
    }
}
