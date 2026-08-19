package com.amirrezahadipoor.falhafez.data.local.seed

import android.content.Context
import com.amirrezahadipoor.falhafez.data.local.PoemDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the bundled poem corpus from assets into Room on first launch.
 * Runs entirely offline; every poem, interpretation and verse ships with the APK.
 */
@Singleton
class CorpusSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poemDao: PoemDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    private val corpusFiles = listOf(
        "corpus/hafez.json",
        "corpus/saadi.json",
        "corpus/rumi.json",
        "corpus/khayyam.json"
    )

    suspend fun seedIfNeeded() = mutex.withLock {
        if (poemDao.count() > 0) return@withLock
        val poems = mutableListOf<SeedPoem>()
        for (file in corpusFiles) {
            runCatching {
                val text = context.assets.open(file)
                    .bufferedReader(Charsets.UTF_8).use { it.readText() }
                poems += json.decodeFromString<List<SeedPoem>>(text)
            }
        }
        if (poems.isEmpty()) return@withLock
        poemDao.insertPoems(poems.map { it.toEntity() })
        poemDao.insertVerses(poems.flatMap { it.toVerses() })
        poemDao.insertFts(poems.map { it.toFts() })
    }
}
