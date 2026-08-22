package ir.siliksama.falhafez.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PoemEntity::class,
        VerseEntity::class,
        PoemFtsEntity::class,
        DrawRecordEntity::class,
        FavoriteEntity::class,
        ReadEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FalDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun drawDao(): DrawDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun readDao(): ReadDao

    companion object {
        /** v1 → v2: add per-beit meaning (معنی بیت) column. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE verses ADD COLUMN meaning TEXT")
            }
        }

        /** v2 → v3: جدولِ «خوانده‌شده‌ها» (علامت خواندن). */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS read_poems (" +
                        "poemId INTEGER NOT NULL PRIMARY KEY, " +
                        "createdAt INTEGER NOT NULL)"
                )
            }
        }

        /**
         * v3 → v4: جایگزینیِ داستان‌های قدیمی با «جهان».
         * داستان‌های گلستان (collection='stories') حذف می‌شوند تا seed جدیدِ
         * «جهان» (۵۰ مطلب) در راه‌اندازیِ بعدی جایگزین شود؛ ارجاع‌های
         * یتیم در تاریخچه/علاقه‌مندی‌ها/خوانده‌شده‌ها/جستجو هم پاک می‌شوند.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val ids = "SELECT id FROM poems WHERE collection = 'stories'"
                db.execSQL("DELETE FROM verses WHERE poemId IN ($ids)")
                db.execSQL("DELETE FROM poems_fts WHERE rowid IN ($ids)")
                db.execSQL("DELETE FROM favorites WHERE poemId IN ($ids)")
                db.execSQL("DELETE FROM draws WHERE poemId IN ($ids)")
                db.execSQL("DELETE FROM read_poems WHERE poemId IN ($ids)")
                db.execSQL("DELETE FROM poems WHERE collection = 'stories'")
            }
        }
    }
}
