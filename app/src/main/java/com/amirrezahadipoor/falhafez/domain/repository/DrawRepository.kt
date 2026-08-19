package com.amirrezahadipoor.falhafez.domain.repository

import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import kotlinx.coroutines.flow.Flow

interface DrawRepository {
    suspend fun record(poemId: Long, question: String?, category: FalCategory): Long
    fun observeHistory(): Flow<List<DrawEntry>>
    suspend fun recentPoemIds(limit: Int = 30): List<Long>
    suspend fun countSince(sinceEpochMillis: Long): Int
}
