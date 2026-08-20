package ir.siliksama.falhafez.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.presentation.components.FavoriteState
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
