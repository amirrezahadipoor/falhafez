package ir.siliksama.falhafez.data.local.seed

import android.content.Context
import android.util.Log
import ir.siliksama.falhafez.data.local.PoemDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the bundled poem corpus from assets into Room on first launch.
 * Runs entirely offline; every poem, interpretation and verse ships with the APK.
 * Hafez is seeded first so the app is usable immediately while the rest continues.
 */
@Singleton
class CorpusSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poemDao: PoemDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    // Hafez FIRST — the app becomes ready (count > 0) as soon as the Divan commits.
    // gzip شده در زمان build تا حجم APK به حداقل برسد؛ اینجا decompress می‌شود.
    private val corpusFiles = listOf(
        "corpus/hafez.json.gz",
        "corpus/khayyam.json.gz",
        "corpus/saadi.json.gz",
        "corpus/rumi.json.gz",
        "corpus/stories.json.gz"
    )

    suspend fun seedIfNeeded() = mutex.withLock {
        if (poemDao.count() > 0) return@withLock

        var total = 0
        var any = false
        for (file in corpusFiles) {
            val batch = runCatching {
                val text = java.util.zip.GZIPInputStream(context.assets.open(file))
                    .bufferedReader(Charsets.UTF_8).use { it.readText() }
                json.decodeFromString<List<SeedPoem>>(text)
            }.getOrDefault(emptyList())

            if (batch.isEmpty()) continue
            any = true
            total += batch.size
            // Insert file-by-file (each in its own transaction) so Hafez commits first.
            poemDao.insertPoems(batch.map { it.toEntity() })
            poemDao.insertVerses(batch.flatMap { it.toVerses() })
            poemDao.insertFts(batch.map { it.toFts() })
        }

        if (!any) Log.w("FalHafez", "Corpus seeding: no poems loaded from assets")
        else Log.i("FalHafez", "Corpus seeded: $total poems")
    }
}
