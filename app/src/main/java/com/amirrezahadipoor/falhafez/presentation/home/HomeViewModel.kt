package com.amirrezahadipoor.falhafez.presentation.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amirrezahadipoor.falhafez.core.sound.Sounds
import com.amirrezahadipoor.falhafez.core.theme.FalThemeId
import com.amirrezahadipoor.falhafez.data.ads.AdManager
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Poet
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import com.amirrezahadipoor.falhafez.domain.repository.PoemRepository
import com.amirrezahadipoor.falhafez.domain.repository.FavoriteRepository
import com.amirrezahadipoor.falhafez.domain.repository.SettingsRepository
import com.amirrezahadipoor.falhafez.domain.usecase.DailyFalUseCase
import com.amirrezahadipoor.falhafez.domain.usecase.DrawFalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val DAILY_FREE_LIMIT = 10
private const val COOLDOWN_MS = 8_000L

enum class DrawStage { NIYYAT, DRAWING, REVEAL, INTERPRETATION }

data class HomeUiState(
    val question: String = "",
    val category: FalCategory = FalCategory.NONE,
    val falSource: Poet? = Poet.HAFEZ,
    val sourceCount: Int = 495,
    val stage: DrawStage = DrawStage.NIYYAT,
    val lastDraw: DrawEntry? = null,
    val dailyFal: Poem? = null,
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
    private val dailyFal: DailyFalUseCase,
    private val drawRepository: DrawRepository,
    private val poemRepository: PoemRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository,
    private val adManager: AdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** The poem whose favorite state we're showing (last draw or the daily fal). */
    private val favoriteTargetId = MutableStateFlow<Long?>(null)

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val isFavorite: StateFlow<Boolean> = favoriteTargetId
        .flatMapLatest { id ->
            if (id == null) flowOf(false) else favoriteRepository.observeIsFavorite(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var counts: Map<Poet, Int> = emptyMap()

    init {
        viewModelScope.launch {
            recomputeRemaining()
            counts = Poet.entries.associateWith { poemRepository.countForPoet(it) }
            _uiState.update { it.copy(sourceCount = counts[it.falSource] ?: 495) }
        }
    }

    fun onQuestionChange(value: String) = _uiState.update { it.copy(question = value) }

    fun onCategorySelect(category: FalCategory) = _uiState.update {
        it.copy(category = if (it.category == category) FalCategory.NONE else category)
    }

    /** Fal source: حافظ / سعدی / مولانا / خیام / همهٔ مجموعه‌ها. */
    fun onSourceSelect(source: Poet?) = _uiState.update {
        val count = when (source) {
            null -> counts.values.sum()
            else -> counts[source] ?: 0
        }
        it.copy(falSource = source, sourceCount = count)
    }

    fun draw() {
        val s = _uiState.value
        if (!s.canDraw) return
        viewModelScope.launch { performDraw(bypassCooldown = false) }
    }

    /** Rewarded hook — grants an extra free draw beyond the daily limit. */
    fun requestExtraDraw(activity: Activity) {
        viewModelScope.launch {
            adManager.showRewarded(activity) {
                viewModelScope.launch {
                    settingsRepository.addRewardedDraw(1)
                    recomputeRemaining()
                    performDraw(bypassCooldown = true)
                }
            }
        }
    }

    /** Rewarded hook — skips the 8s repeat cooldown and draws immediately. */
    fun requestSkipCooldown(activity: Activity) {
        viewModelScope.launch {
            adManager.showRewarded(activity) {
                viewModelScope.launch { performDraw(bypassCooldown = true) }
            }
        }
    }

    private suspend fun performDraw(bypassCooldown: Boolean) {
        val s = _uiState.value
        if (s.busy || s.stage == DrawStage.DRAWING) return
        if (!bypassCooldown && s.cooldownActive) return
        _uiState.update { it.copy(busy = true, stage = DrawStage.DRAWING) }
        Sounds.draw()

        val question = s.question.trim().ifBlank { null }
        val result = drawFal(question, s.category, s.falSource)
        favoriteTargetId.value = result?.poem?.id
        _uiState.update { it.copy(lastDraw = result, dailyFal = null, cooldownActive = true, busy = false) }
        recomputeRemaining()
        adManager.onDrawCompleted()

        viewModelScope.launch {
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

    fun onDrawingFinished() {
        Sounds.reveal()
        _uiState.update { it.copy(stage = DrawStage.REVEAL) }
    }

    fun onReadInterpretation() = _uiState.update { it.copy(stage = DrawStage.INTERPRETATION) }

    /** فالِ روز — deterministic, shared by everyone on the same day. */
    fun openDailyFal() {
        viewModelScope.launch {
            val poem = dailyFal.today()
            if (poem != null) {
                favoriteTargetId.value = poem.id
                _uiState.update { it.copy(dailyFal = poem) }
            }
        }
    }

    fun closeDailyFal() {
        _uiState.update { it.copy(dailyFal = null) }
        favoriteTargetId.value = _uiState.value.lastDraw?.poem?.id
    }

    fun onToggleFavorite() {
        val poemId = favoriteTargetId.value ?: return
        viewModelScope.launch { favoriteRepository.toggle(poemId) }
    }

    fun dismissAndMaybeAd(activity: Activity) {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null) }
        favoriteTargetId.value = null
        viewModelScope.launch { adManager.showInterstitial(activity) }
    }

    fun dismissOnly() {
        _uiState.update { it.copy(stage = DrawStage.NIYYAT, lastDraw = null) }
        favoriteTargetId.value = null
    }
}
