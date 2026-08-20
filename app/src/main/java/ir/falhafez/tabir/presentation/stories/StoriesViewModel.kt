package ir.falhafez.tabir.presentation.stories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.falhafez.tabir.core.theme.FalThemeId
import ir.falhafez.tabir.domain.model.Collection
import ir.falhafez.tabir.domain.model.Poem
import ir.falhafez.tabir.domain.repository.PoemRepository
import ir.falhafez.tabir.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _stories = MutableStateFlow<List<Poem>>(emptyList())
    val stories: StateFlow<List<Poem>> = _stories.asStateFlow()

    private val _selected = MutableStateFlow<Poem?>(null)
    val selected: StateFlow<Poem?> = _selected.asStateFlow()

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    init {
        viewModelScope.launch {
            _stories.value = poemRepository.getPoemsByCollection(Collection.STORIES)
        }
    }

    fun open(story: Poem) {
        _selected.value = story
    }

    fun close() {
        _selected.value = null
    }
}
