package ir.siliksama.falhafez.domain.repository

import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import kotlinx.coroutines.flow.Flow

interface PoemRepository {
    suspend fun getPoem(id: Long): Poem?
    suspend fun getPoemsByPoet(poet: Poet): List<Poem>
    suspend fun getPoemsByCollection(collection: Collection): List<Poem>
    suspend fun search(query: String): List<Poem>
    /**
     * Draws a random poem — by default only from the Divan of Hafez (the fal source).
     * Pass another [poet] explicitly if a different source is ever needed.
     */
    /** Null [poet] = draw from ALL collections (حافظ + سعدی + مولانا + خیام). */
    suspend fun getRandomPoem(excludeIds: List<Long> = emptyList(), poet: Poet? = Poet.HAFEZ): Poem?
    /** Deterministic pick for فالِ روز — same poem for everyone on a given day (Hafez only). */
    suspend fun getPoemAt(poet: Poet, index: Int): Poem?
    suspend fun countForPoet(poet: Poet): Int
    suspend fun count(): Int
    fun observeCount(): Flow<Int>
}
