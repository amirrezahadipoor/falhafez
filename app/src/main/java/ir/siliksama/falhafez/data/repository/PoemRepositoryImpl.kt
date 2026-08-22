package ir.siliksama.falhafez.data.repository

import ir.siliksama.falhafez.core.util.SearchSanitizer
import ir.siliksama.falhafez.data.local.PoemDao
import ir.siliksama.falhafez.data.local.PoemWithVerses
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.model.Verse
import ir.siliksama.falhafez.domain.repository.PoemRepository
import ir.siliksama.falhafez.domain.usecase.FalLottery
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
        // حفاظِ کراش: فقط حروف/رقم/فاصله و نیم‌فاصله می‌مانند — عملگرهای FTS
        // («(»، «)»، «-»، «"»، «:»، «^» و…) حذف می‌شوند تا MATCH هرگز با عبارتِ
        // نادرست خطای «malformed MATCH expression» ندهد. (منطق در SearchSanitizer
        // که با آزمون واحد قفل شده است.)
        val matchQuery = SearchSanitizer.sanitize(query) ?: return emptyList()
        return runCatching { poemDao.search(matchQuery).mapWithVerses() }.getOrDefault(emptyList())
    }

    /**
     * قرعهٔ فال — وزن‌دار، نه کاملاً تصادفی. منطقِ وزن‌دهی در [FalLottery].
     *
     * `poet == null` یعنی «همهٔ مجموعه‌ها»؛ در این حالت سهمِ شاعران متعادل می‌شود،
     * وگرنه انتخابِ تصادفیِ ساده عملاً فقط مولانا می‌داد (۷۴.۸٪ استخر).
     */
    override suspend fun getRandomPoem(excludeIds: List<Long>, poet: Poet?): Poem? {
        val weightByPoet = poet == null

        var candidates = if (weightByPoet) {
            poemDao.getCandidatesWithSize(excludeIds)
        } else {
            poemDao.getCandidatesWithSizeForPoet(poet.key, excludeIds)
        }

        // اگر حذفِ فال‌های اخیر استخر را خالی کرد، بدونِ حذف دوباره تلاش می‌کنیم.
        if (candidates.isEmpty()) {
            candidates = if (weightByPoet) {
                poemDao.getCandidatesWithSize(emptyList())
            } else {
                poemDao.getCandidatesWithSizeForPoet(poet.key, emptyList())
            }
        }
        if (candidates.isEmpty()) return null

        val chosen = FalLottery.pick(
            items = candidates.map { Triple(it.id, it.poet, it.beits) },
            weightByPoet = weightByPoet
        ) ?: return null
        return getPoem(chosen)
    }

    override suspend fun getPoemAt(poet: Poet, index: Int): Poem? {
        val id = poemDao.getPoemIdAtForPoetCollection(poet.key, "ghazal", index) ?: return null
        return getPoem(id)
    }

    override suspend fun countForPoetCollection(poet: Poet, collection: Collection): Int =
        poemDao.countForPoetCollection(poet.key, collection.key)

    override suspend fun getPoemAtForCollection(poet: Poet, collection: Collection, index: Int): Poem? {
        val id = poemDao.getPoemIdAtForPoetCollection(poet.key, collection.key, index) ?: return null
        return getPoem(id)
    }

    override suspend fun countForPoet(poet: Poet): Int = poemDao.countForPoet(poet.key)

    override suspend fun count(): Int = poemDao.count()

    override fun observeCount(): Flow<Int> = poemDao.observeCount()

    private suspend fun List<ir.siliksama.falhafez.data.local.PoemEntity>.mapWithVerses(): List<Poem> {
        if (isEmpty()) return emptyList()
        // حفاظِ کراش: پرس‌وجوهای IN به قطعاتِ کوچک تقسیم می‌شوند — دستگاه‌های قدیمی
        // (SQLite < 3.32، اندروید ≤ 10) سقفِ ۹۹۹ متغیر دارند و «دیوان شمس» ۳۲۷۴ شعر دارد.
        val ids = map { it.id }
        val verses = buildList {
            ids.chunked(400).forEach { chunk ->
                addAll(poemDao.getVersesForIds(chunk))
            }
        }
        val grouped = verses.groupBy { it.poemId }
        return map { entity ->
            val vs = grouped[entity.id].orEmpty().sortedBy { it.position }
            entity.toDomain(vs)
        }
    }

    private fun ir.siliksama.falhafez.data.local.PoemEntity.toDomain(
        verses: List<ir.siliksama.falhafez.data.local.VerseEntity>
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
