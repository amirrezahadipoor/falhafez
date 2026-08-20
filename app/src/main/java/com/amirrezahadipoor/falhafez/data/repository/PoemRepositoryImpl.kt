package com.amirrezahadipoor.falhafez.data.repository

import com.amirrezahadipoor.falhafez.data.local.PoemDao
import com.amirrezahadipoor.falhafez.data.local.PoemWithVerses
import com.amirrezahadipoor.falhafez.domain.model.Collection
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Poet
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.domain.repository.PoemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PoemRepositoryImpl @Inject constructor(
    private val poemDao: PoemDao
) : PoemRepository {

    override suspend fun getPoem(id: Long): Poem? =
        poemDao.getPoemWithVerses(id)?.toDomain()

    override suspend fun getPoemsByPoet(poet: Poet): List<Poem> =
        poemDao.getByPoet(poet.key).mapWithVerses()

    override suspend fun getPoemsByCollection(collection: Collection): List<Poem> =
        poemDao.getByCollection(collection.key).mapWithVerses()

    override suspend fun search(query: String): List<Poem> {
        val safe = query.trim().replace("\"", "").replace("*", "")
        if (safe.isBlank()) return emptyList()
        val matchQuery = safe.split(Regex("\\s+")).joinToString(" ") { "$it*" }
        return poemDao.search(matchQuery).mapWithVerses()
    }

    override suspend fun getRandomPoem(excludeIds: List<Long>): Poem? {
        val candidates = poemDao.getCandidateIds(excludeIds)
        val pool = candidates.ifEmpty { poemDao.getAllPoemIds() }
        val chosen = pool.randomOrNull() ?: return null
        return getPoem(chosen)
    }

    override suspend fun getPoemAt(index: Int): Poem? {
        val id = poemDao.getPoemIdAt(index) ?: return null
        return getPoem(id)
    }

    override suspend fun count(): Int = poemDao.count()

    override fun observeCount(): Flow<Int> = poemDao.observeCount()

    private suspend fun List<com.amirrezahadipoor.falhafez.data.local.PoemEntity>.mapWithVerses(): List<Poem> {
        if (isEmpty()) return emptyList()
        val verses = poemDao.getVersesForIds(map { it.id })
        val grouped = verses.groupBy { it.poemId }
        return map { entity ->
            val vs = grouped[entity.id].orEmpty().sortedBy { it.position }
            entity.toDomain(vs)
        }
    }

    private fun com.amirrezahadipoor.falhafez.data.local.PoemEntity.toDomain(
        verses: List<com.amirrezahadipoor.falhafez.data.local.VerseEntity>
    ): Poem = Poem(
        id = id,
        poet = Poet.fromKey(poet),
        collection = Collection.fromKey(collection) ?: Collection.HAFEZ_GHAZAL,
        number = number,
        themeTag = themeTag,
        tafsir = tafsir,
        verses = verses.map {
            Verse(position = it.position, first = it.first, second = it.second, meaning = it.meaning)
        }
    )

    private fun PoemWithVerses.toDomain(): Poem =
        poem.toDomain(verses.sortedBy { it.position })
}
