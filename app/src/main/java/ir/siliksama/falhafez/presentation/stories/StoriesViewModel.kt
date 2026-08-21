package ir.siliksama.falhafez.presentation.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.repository.ReadRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val readRepository: ReadRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _stories = MutableStateFlow<List<Poem>>(emptyList())
    val stories: StateFlow<List<Poem>> = _stories.asStateFlow()

    private val _selected = MutableStateFlow<Poem?>(null)
    val selected: StateFlow<Poem?> = _selected.asStateFlow()

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val readIds: StateFlow<Set<Long>> = readRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        load()
    }

    /** بارگذاری با retry — کورپوسِ داستان‌ها آخرین فایلی است که seed می‌شود،
     *  پس اگر صفحه زود باز شود باید صبر کنیم تا داده برسد (بدون نیاز کاربر). */
    fun load() {
        viewModelScope.launch {
            repeat(20) { attempt ->
                val list = poemRepository.getPoemsByCollection(Collection.STORIES)
                if (list.isNotEmpty()) {
                    _stories.value = list
                    return@launch
                }
                delay(600L)
            }
            _stories.value = emptyList()
        }
    }

    fun open(story: Poem) {
        _selected.value = story
        viewModelScope.launch { readRepository.markRead(story.id) }
    }

    fun toggleRead(story: Poem) {
        viewModelScope.launch {
            if (readIds.value.contains(story.id)) readRepository.unmarkRead(story.id)
            else readRepository.markRead(story.id)
        }
    }

    fun close() {
        _selected.value = null
    }
}
