package com.amirrezahadipoor.falhafez.data.repository

import com.amirrezahadipoor.falhafez.data.local.DrawDao
import com.amirrezahadipoor.falhafez.data.local.DrawRecordEntity
import com.amirrezahadipoor.falhafez.data.local.PoemDao
import com.amirrezahadipoor.falhafez.data.local.PoemWithVerses
import com.amirrezahadipoor.falhafez.domain.model.Collection
import com.amirrezahadipoor.falhafez.domain.model.DrawEntry
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Poet
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.domain.repository.DrawRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DrawRepositoryImpl @Inject constructor(
    private val drawDao: DrawDao,
    private val poemDao: PoemDao
) : DrawRepository {

    override suspend fun record(poemId: Long, question: String?, category: FalCategory): Long =
        drawDao.insert(
            DrawRecordEntity(
                poemId = poemId,
                question = question,
                category = if (category == FalCategory.NONE) null else category.key,
                drawnAt = System.currentTimeMillis()
            )
        )

    override fun observeHistory(): Flow<List<DrawEntry>> =
        drawDao.observeAll().map { rows ->
            if (rows.isEmpty()) emptyList()
            else {
                val poems = poemDao.getPoemsWithVerses(rows.map { it.poemId }.distinct())
                val byId = poems.associateBy { it.poem.id }
                rows.mapNotNull { row ->
                    byId[row.poemId]?.let { withVerses ->
                        DrawEntry(
                            id = row.id,
                            poem = withVerses.toDomain(),
                            question = row.question,
                            category = FalCategory.fromKey(row.category),
                            drawnAt = row.drawnAt
                        )
                    }
                }
            }
        }

    override suspend fun recentPoemIds(limit: Int): List<Long> =
        drawDao.recentPoemIds(limit)

    override suspend fun countSince(sinceEpochMillis: Long): Int =
        drawDao.countSince(sinceEpochMillis)

    private fun PoemWithVerses.toDomain(): Poem = Poem(
        id = poem.id,
        poet = Poet.fromKey(poem.poet),
        collection = Collection.fromKey(poem.collection) ?: Collection.HAFEZ_GHAZAL,
        number = poem.number,
        themeTag = poem.themeTag,
        tafsir = poem.tafsir,
        verses = verses.sortedBy { it.position }
            .map {
                Verse(position = it.position, first = it.first, second = it.second, meaning = it.meaning)
            }
    )
}
