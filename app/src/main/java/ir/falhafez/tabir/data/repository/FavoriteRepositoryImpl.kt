package ir.falhafez.tabir.data.repository

import ir.falhafez.tabir.data.local.FavoriteDao
import ir.falhafez.tabir.data.local.FavoriteEntity
import ir.falhafez.tabir.data.local.PoemDao
import ir.falhafez.tabir.data.local.PoemWithVerses
import ir.falhafez.tabir.domain.model.Collection
import ir.falhafez.tabir.domain.model.Poem
import ir.falhafez.tabir.domain.model.Poet
import ir.falhafez.tabir.domain.model.Verse
import ir.falhafez.tabir.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val poemDao: PoemDao
) : FavoriteRepository {

    override suspend fun toggle(poemId: Long): Boolean {
        val existing = favoriteDao.get(poemId)
        return if (existing == null) {
            favoriteDao.insert(FavoriteEntity(poemId = poemId, createdAt = System.currentTimeMillis()))
            true
        } else {
            favoriteDao.delete(poemId)
            false
        }
    }

    override fun observeIsFavorite(poemId: Long): Flow<Boolean> =
        favoriteDao.observe(poemId).map { it != null }

    override fun observeFavorites(): Flow<List<Poem>> =
        favoriteDao.observeAll().map { rows ->
            if (rows.isEmpty()) emptyList()
            else poemDao.getPoemsWithVerses(rows.map { it.poemId })
                .sortedByDescending { pv -> rows.first { it.poemId == pv.poem.id }.createdAt }
                .map { it.toDomain() }
        }

    override suspend fun favoriteIds(): Set<Long> = favoriteDao.favoriteIds().toSet()

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
