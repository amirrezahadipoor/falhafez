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
        FavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FalDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun drawDao(): DrawDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        /** v1 → v2: add per-beit meaning (معنی بیت) column. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE verses ADD COLUMN meaning TEXT")
            }
        }
    }
}
