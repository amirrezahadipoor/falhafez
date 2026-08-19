package com.amirrezahadipoor.falhafez.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.repository.FavoriteRepository
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import com.amirrezahadipoor.falhafez.domain.usecase.DrawFalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val question: String = "",
    val category: FalCategory = FalCategory.NONE,
    val drawing: Boolean = false,
    val lastDraw: DrawEntry? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val drawFal: DrawFalUseCase,
    private val favoriteRepository: FavoriteRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val isFavorite: StateFlow<Boolean> = _uiState
        .map { it.lastDraw?.poem?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(false) else favoriteRepository.observeIsFavorite(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onQuestionChange(value: String) = _uiState.update { it.copy(question = value) }

    fun onCategorySelect(category: FalCategory) = _uiState.update {
        it.copy(category = if (it.category == category) FalCategory.NONE else category)
    }

    fun draw() {
        val current = _uiState.value
        if (current.drawing) return
        viewModelScope.launch {
            _uiState.update { it.copy(drawing = true) }
            val question = current.question.trim().ifBlank { null }
            val result = drawFal(question, current.category)
            _uiState.update { it.copy(drawing = false, lastDraw = result) }
        }
    }

    fun toggleFavorite() {
        val poemId = _uiState.value.lastDraw?.poem?.id ?: return
        viewModelScope.launch { favoriteRepository.toggle(poemId) }
    }

    fun dismissResult() = _uiState.update { it.copy(lastDraw = null) }
}
