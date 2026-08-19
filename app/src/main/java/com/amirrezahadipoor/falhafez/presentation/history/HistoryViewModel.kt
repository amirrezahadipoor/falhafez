package com.amirrezahadipoor.falhafez.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import com.amirrezahadipoor.falhafez.domain.repository.FavoriteRepository
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import com.amirrezahadipoor.falhafez.presentation.components.FavoriteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    drawRepository: DrawRepository,
    favoriteRepository: FavoriteRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val history: StateFlow<List<DrawEntry>> = drawRepository.observeHistory()
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
