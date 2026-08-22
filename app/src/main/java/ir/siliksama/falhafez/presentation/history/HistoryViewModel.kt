package ir.siliksama.falhafez.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.domain.usecase.PersonalizeTafsir
import ir.siliksama.falhafez.presentation.components.FavoriteState
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
    settingsRepository: SettingsRepository,
    private val personalizeTafsir: PersonalizeTafsir
) : ViewModel() {

    val history: StateFlow<List<DrawEntry>> = drawRepository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val favorite = FavoriteState(favoriteRepository, viewModelScope)

    private val _selectedId = MutableStateFlow<Long?>(null)
    val selectedId: StateFlow<Long?> = _selectedId.asStateFlow()

    private val _showStats = MutableStateFlow(false)
    val showStats: StateFlow<Boolean> = _showStats.asStateFlow()

    fun toggleStats() {
        _showStats.value = !_showStats.value
    }

    fun open(id: Long) {
        _selectedId.value = id
        favorite.select(id)
    }

    fun close() {
        _selectedId.value = null
        favorite.select(null)
    }

    /**
     * تفسیرِ شخصی‌شدهٔ یک فالِ بایگانی‌شده.
     *
     * چون بذرِ [PersonalizeTafsir] شناسهٔ همان فال است، متن **دقیقاً همانی** است که
     * کاربر لحظهٔ گرفتنِ فال دید. پیش‌تر تاریخچه تفسیرِ خام را نشان می‌داد، پس فالی
     * که کاربر ذخیره کرده بود، وقتی دوباره بازش می‌کرد متنِ دیگری داشت.
     */
    fun personalTafsirFor(entry: DrawEntry): String = runCatching {
        personalizeTafsir(
            poem = entry.poem,
            question = entry.question,
            category = entry.category,
            seed = entry.id
        )
    }.getOrDefault(entry.poem.tafsir)
}
