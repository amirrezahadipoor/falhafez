package ir.siliksama.falhafez.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReadRepository {
    fun observeIds(): Flow<Set<Long>>
    fun observeIsRead(poemId: Long): Flow<Boolean>
    suspend fun markRead(poemId: Long)
    suspend fun unmarkRead(poemId: Long)
}
