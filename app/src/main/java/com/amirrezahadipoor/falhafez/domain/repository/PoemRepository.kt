package com.amirrezahadipoor.falhafez.domain.repository

import com.amirrezahadipoor.falhafez.domain.model.Collection
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Poet
import kotlinx.coroutines.flow.Flow

interface PoemRepository {
    suspend fun getPoem(id: Long): Poem?
    suspend fun getPoemsByPoet(poet: Poet): List<Poem>
    suspend fun getPoemsByCollection(collection: Collection): List<Poem>
    suspend fun search(query: String): List<Poem>
    suspend fun getRandomPoem(excludeIds: List<Long> = emptyList()): Poem?
    /** Deterministic pick (used by فالِ روز — same poem for everyone on a given day). */
    suspend fun getPoemAt(index: Int): Poem?
    suspend fun count(): Int
    fun observeCount(): Flow<Int>
}
