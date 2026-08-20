package ir.siliksama.falhafez.presentation.components

import ir.siliksama.falhafez.domain.repository.FavoriteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Tracks the favorite state of the currently-selected poem. Shared by the
 * history/favorites/library screens to avoid duplicating per-poem Flow logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteState(
    private val favoriteRepository: FavoriteRepository,
    private val scope: CoroutineScope
) {
    private val selectedId = MutableStateFlow<Long?>(null)

    val isSelectedFavorite: StateFlow<Boolean> = selectedId
        .flatMapLatest { id ->
            if (id == null) flowOf(false) else favoriteRepository.observeIsFavorite(id)
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    fun select(id: Long?) {
        selectedId.value = id
    }

    fun toggleSelected() {
        scope.launch { selectedId.value?.let { favoriteRepository.toggle(it) } }
    }
}
