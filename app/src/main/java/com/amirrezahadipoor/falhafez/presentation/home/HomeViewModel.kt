package com.amirrezahadipoor.falhafez.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import com.amirrezahadipoor.falhafez.domain.repository.FavoriteRepository
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import com.amirrezahadipoor.falhafez.domain.usecase.DrawFalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Free draws allowed per day (offline policy — a rewarded-ad hook extends this, see AdManager). */
private const val DAILY_FREE_LIMIT = 10

/** Soft cooldown between consecutive draws to protect the ritual feeling. */
private const val COOLDOWN_MS = 8_000L

enum class DrawStage { NIYYAT, DRAWING, REVEAL, INTERPRETATION }

data class HomeUiState(
    val question: String = "",
    val category: FalCategory = FalCategory.NONE,
    val stage: DrawStage = DrawStage.NIYYAT,
    val lastDraw: DrawEntry? = null,
    val cooldownActive: Boolean = false,
    val remainingToday: Int = DAILY_FREE_LIMIT,
    val busy: Boolean = false
) {
    val canDraw: Boolean
        get() = !busy && !cooldownActive && remainingToday > 0 && stage != DrawStage.DRAWING
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val drawFal: DrawFalUseCase,
    private val drawRepository: DrawRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository
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

    init {
        viewModelScope.launch { recomputeRemaining() }
    }

    fun onQuestionChange(value: String) = _uiState.update { it.copy(question = value) }

    fun onCategorySelect(category: FalCategory) = _uiState.update {
        it.copy(category = if (it.category == category) FalCategory.NONE else category)
    }

    fun draw() {
        val s = _uiState.value
        if (!s.canDraw) return
        viewModelScope.launch { performDraw(bypassCooldown = false) }
    }

    /** Reward hook — an extra draw that also bypasses the repeat cooldown. */
    fun grantExtraDraw() {
        viewModelScope.launch {
            settingsRepository.addRewardedDraw(1)
            recomputeRemaining()
            performDraw(bypassCooldown = true)
        }
    }

    private suspend fun performDraw(bypassCooldown: Boolean) {
        val s = _uiState.value
        if (s.busy || s.stage == DrawStage.DRAWING) return
        if (!bypassCooldown && s.cooldownActive) return
        _uiState.update { it.copy(busy = true, stage = DrawStage.DRAWING) }

        val question = s.question.trim().ifBlank { null }
        val result = drawFal(question, s.category)
        _uiState.update { it.copy(lastDraw = result, cooldownActive = true, busy = false) }
        recomputeRemaining()

        launch {
            delay(COOLDOWN_MS)
            _uiState.update { it.copy(cooldownActive = false) }
        }
    }

    private suspend fun recomputeRemaining() {
        val used = drawRepository.countSince(startOfToday())
        val extra = settingsRepository.rewardedExtraDraws.first()
        _uiState.update { it.copy(remainingToday = DAILY_FREE_LIMIT - used + extra) }
    }

    private fun startOfToday(): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun onDrawingFinished() = _uiState.update { it.copy(stage = DrawStage.REVEAL) }

    fun onReadInterpretation() = _uiState.update { it.copy(stage = DrawStage.INTERPRETATION) }

    fun onToggleFavorite() {
        val poemId = _uiState.value.lastDraw?.poem?.id ?: return
        viewModelScope.launch { favoriteRepository.toggle(poemId) }
    }

    fun onDismiss() = _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null) }
}
