package ir.siliksama.falhafez.data.repository

import ir.siliksama.falhafez.data.local.DrawDao
import ir.siliksama.falhafez.data.local.DrawRecordEntity
import ir.siliksama.falhafez.data.local.PoemDao
import ir.siliksama.falhafez.data.local.PoemWithVerses
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.model.Verse
import ir.siliksama.falhafez.domain.repository.DrawRepository
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
                // حفاظِ کراش: chunked تا سقفِ ۹۹۹ متغیرِ SQLite در دستگاه‌های قدیمی رد نشود.
                val poems = buildList {
                    rows.map { it.poemId }.distinct().chunked(400).forEach { chunk ->
                        addAll(poemDao.getPoemsWithVerses(chunk))
                    }
                }
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
