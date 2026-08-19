package com.amirrezahadipoor.falhafez.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PoemEntity::class,
        VerseEntity::class,
        PoemFtsEntity::class,
        DrawRecordEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FalDatabase : RoomDatabase() {
    abstract fun poemDao(): PoemDao
    abstract fun drawDao(): DrawDao
    abstract fun favoriteDao(): FavoriteDao
}
