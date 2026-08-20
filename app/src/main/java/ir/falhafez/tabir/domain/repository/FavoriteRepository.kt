package ir.falhafez.tabir.domain.repository

import ir.falhafez.tabir.domain.model.Poem
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    /** Toggles favorite state; returns the new state (true = now favorite). */
    suspend fun toggle(poemId: Long): Boolean
    fun observeIsFavorite(poemId: Long): Flow<Boolean>
    fun observeFavorites(): Flow<List<Poem>>
    suspend fun favoriteIds(): Set<Long>
}
