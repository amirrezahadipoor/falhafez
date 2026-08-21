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
    version = 3,
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
    }
}
