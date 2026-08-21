package ir.siliksama.falhafez.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.siliksama.falhafez.core.theme.FalThemeId
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.repository.ReadRepository
import ir.siliksama.falhafez.domain.repository.SettingsRepository
import ir.siliksama.falhafez.presentation.components.FavoriteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val query: String = "",
    val poet: Poet? = null,
    val collection: Collection? = null,
    val poems: List<Poem> = emptyList(),
    val loading: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val poemRepository: PoemRepository,
    private val readRepository: ReadRepository,
    favoriteRepository: FavoriteRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _detail = MutableStateFlow<Poem?>(null)
    val detail: StateFlow<Poem?> = _detail.asStateFlow()

    val themeId: StateFlow<FalThemeId> = settingsRepository.themeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FalThemeId.TAZHIB)

    val favorite = FavoriteState(favoriteRepository, viewModelScope)

    /** مجموعهٔ شعرهای خوانده‌شده — برای نشانِ «خوانده‌شده» در فهرست‌ها. */
    val readIds: StateFlow<Set<Long>> = readRepository.observeIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val searchResults: StateFlow<List<Poem>> = _uiState
        .map { it.query }
        .debounce(250)
        .flatMapLatest { q -> flow { emit(poemRepository.search(q)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }

    fun openPoet(poet: Poet) = _uiState.update {
        it.copy(poet = poet, collection = null, poems = emptyList())
    }

    fun openCollection(collection: Collection) {
        _uiState.update { it.copy(collection = collection, poems = emptyList()) }
        load()
    }

    private fun load() {
        val c = _uiState.value.collection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            // retry — اگر صفحه قبل از پایانِ seed باز شود، داده به‌زودی می‌رسد.
            var list = emptyList<Poem>()
            repeat(20) {
                list = poemRepository.getPoemsByCollection(c)
                if (list.isNotEmpty()) return@launch _uiState.update { it.copy(poems = list, loading = false) }
                delay(600L)
            }
            _uiState.update { it.copy(poems = list, loading = false) }
        }
    }

    fun openPoem(poem: Poem) {
        _detail.value = poem
        favorite.select(poem.id)
        viewModelScope.launch { readRepository.markRead(poem.id) }
    }

    fun toggleRead(poem: Poem) {
        viewModelScope.launch {
            if (readIds.value.contains(poem.id)) {
                readRepository.unmarkRead(poem.id)
            } else {
                readRepository.markRead(poem.id)
            }
        }
    }

    fun openPoemById(id: Long) {
        viewModelScope.launch {
            val poem = poemRepository.getPoem(id)
            if (poem != null) {
                _detail.value = poem
                favorite.select(id)
            }
        }
    }

    fun back() {
        when {
            _detail.value != null -> {
                _detail.value = null
                favorite.select(null)
            }
            _uiState.value.collection != null ->
                _uiState.update { it.copy(collection = null, poems = emptyList()) }
            _uiState.value.poet != null ->
                _uiState.update { it.copy(poet = null) }
        }
    }
}
