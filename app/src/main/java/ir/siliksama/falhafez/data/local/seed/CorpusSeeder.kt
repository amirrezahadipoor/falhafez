package ir.siliksama.falhafez.data.local.seed

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.siliksama.falhafez.data.local.PoemDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.corpusDataStore by preferencesDataStore(name = "corpus_state")

/**
 * بارگذاریِ دیوان‌های همراهِ APK در Room.
 *
 * کاملاً آفلاین: هر شعر، هر بیت و هر تفسیر داخلِ خودِ برنامه است.
 * حافظ اول seed می‌شود تا اپ در همان ثانیه‌های اول قابلِ استفاده باشد.
 */
@Singleton
class CorpusSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val poemDao: PoemDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    private object Keys {
        val REVISION = intPreferencesKey("corpus_revision")
    }

    // حافظ اول — به‌محضِ درجِ دیوانِ حافظ، اپ آمادهٔ فال گرفتن است.
    // فایل‌ها با gzip فشرده و با پسوند .dat ذخیره شده‌اند (aapt2 دوباره فشرده‌شان نمی‌کند).
    private val corpusFiles = listOf(
        "corpus/hafez.dat",
        "corpus/khayyam.dat",
        "corpus/saadi.dat",
        "corpus/rumi.dat",
        "corpus/stories.dat"
    )

    suspend fun seedIfNeeded() = mutex.withLock {
        val storedRevision = runCatching {
            context.corpusDataStore.data.first()[Keys.REVISION] ?: 0
        }.getOrDefault(0)

        // ── نصبِ تازه ──
        if (poemDao.count() == 0) {
            var total = 0
            for (file in corpusFiles) total += seedFile(file)
            markRevision()
            Log.i(TAG, "Corpus seeded: $total poems (revision $CORPUS_REVISION)")
            return@withLock
        }

        // ── به‌روزرسانیِ متن‌ها ──
        // وقتی تفسیرها/معنی ابیات بازنویسی می‌شوند، نسخه بالا می‌رود و متنِ روی دستگاه
        // تازه می‌شود. **فقط متن** جایگزین می‌شود؛ تاریخچه، علاقه‌مندی‌ها و
        // خوانده‌شده‌ها دست نمی‌خورند چون شناسهٔ شعرها ثابت است.
        if (storedRevision < CORPUS_REVISION) {
            var updated = 0
            for (file in corpusFiles) updated += seedFile(file)
            markRevision()
            Log.i(TAG, "Corpus text refreshed: $updated poems ($storedRevision → $CORPUS_REVISION)")
            return@withLock
        }

        // ── ترمیمِ بخشِ «جهان» اگر خالی مانده باشد ──
        if (poemDao.countByCollection("stories") == 0) {
            val n = seedFile("corpus/stories.dat")
            if (n > 0) Log.i(TAG, "«جهان» reseeded: $n items")
        }
    }

    private suspend fun markRevision() {
        runCatching {
            context.corpusDataStore.edit { it[Keys.REVISION] = CORPUS_REVISION }
        }
    }

    /**
     * یک فایلِ کورپوس را می‌خواند و درج می‌کند.
     *
     * ⚠️ نکتهٔ حافظه: `rumi.dat` باز شده حدود ۲۶ مگابایت است. نسخهٔ قبلی کلِ آن را با
     * `readText()` به یک `String` می‌خواند و بعد پارس می‌کرد — یعنی هم‌زمان رشتهٔ
     * ۲۶ مگابایتی + درختِ کاملِ اشیاء در حافظه بود (علتِ نیاز به `largeHeap`).
     * اینجا مستقیم از استریم پارس می‌شود و درج به دسته‌های [CHUNK] شکسته شده است.
     */
    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun seedFile(file: String): Int {
        val batch = runCatching {
            context.assets.open(file).use { raw ->
                GZIPInputStream(raw, 32 * 1024).use { gz ->
                    json.decodeFromStream<List<SeedPoem>>(gz)
                }
            }
        }.getOrElse {
            Log.e(TAG, "Seeding: failed to read $file", it)
            emptyList()
        }

        if (batch.isEmpty()) {
            Log.w(TAG, "Seeding: no poems loaded from $file")
            return 0
        }

        var inserted = 0
        // درجِ تکه‌تکه: به‌جای یک تراکنشِ غول‌آسا برای ۸٬۵۱۵ شعر و ۸۴٬۵۰۶ بیت،
        // دسته‌های کوچک — اوجِ مصرفِ حافظه پایین می‌آید و اگر چیزی خطا داد،
        // فقط همان دسته از دست می‌رود نه کلِ فایل.
        batch.chunked(CHUNK).forEach { chunk ->
            runCatching {
                poemDao.insertPoems(chunk.map { it.toEntity() })
                poemDao.insertVerses(chunk.flatMap { it.toVerses() })
                poemDao.insertFts(chunk.map { it.toFts() })
                inserted += chunk.size
            }.onFailure { Log.e(TAG, "Seeding chunk failed for $file", it) }
        }
        return inserted
    }

    companion object {
        private const val TAG = "FalHafez"
        private const val CHUNK = 500

        /**
         * نسخهٔ محتوای دیوان‌ها.
         *
         * **هر بار که فایل‌های `assets/corpus/*.dat` بازسازی می‌شوند این عدد باید یکی
         * زیاد شود**، وگرنه کاربرانِ فعلی متنِ قدیمی را می‌بینند.
         *
         * ۲ = بازنویسیِ کاملِ تفسیرها (حذفِ ۷٬۶۸۴ تفسیرِ قالبی)، پاک‌سازیِ ۶٬۴۲۰ شرحِ
         *     بیت، و اصلاحِ انتسابِ بخشِ «جهان» از سعدی به «اندیشهٔ جهان».
         */
        const val CORPUS_REVISION = 2
    }
}
