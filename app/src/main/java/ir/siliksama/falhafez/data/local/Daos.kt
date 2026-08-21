package ir.siliksama.falhafez.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {
    @Query("SELECT * FROM poems WHERE id = :id")
    suspend fun getPoem(id: Long): PoemEntity?

    @Transaction
    @Query("SELECT * FROM poems WHERE id = :id")
    suspend fun getPoemWithVerses(id: Long): PoemWithVerses?

    @Transaction
    @Query("SELECT * FROM poems WHERE id IN (:ids)")
    suspend fun getPoemsWithVerses(ids: List<Long>): List<PoemWithVerses>

    @Query("SELECT * FROM poems WHERE poet = :poet ORDER BY number")
    suspend fun getByPoet(poet: String): List<PoemEntity>

    @Query("SELECT * FROM poems WHERE collection = :collection ORDER BY number")
    suspend fun getByCollection(collection: String): List<PoemEntity>

    @Query("SELECT * FROM verses WHERE poemId = :poemId ORDER BY position")
    suspend fun getVerses(poemId: Long): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE poemId IN (:ids) ORDER BY poemId, position")
    suspend fun getVersesForIds(ids: List<Long>): List<VerseEntity>

    @Query("SELECT id FROM poems")
    suspend fun getAllPoemIds(): List<Long>

    // چهار شعرِ ملحقاتِ سعدی که متنِ دست‌نویسشان ناقص است (جایِ واژه‌ها «…» است)
    // در فالگیری شرکت نمی‌کنند — فال باید همیشه از متنِ کامل باشد — ولی در کتابخانه می‌مانند.
    @Query(
        "SELECT id FROM poems WHERE collection != 'stories' AND id NOT IN (:exclude) " +
            "AND id NOT IN (10716, 10717, 10724, 10729)"
    )
    suspend fun getCandidateIds(exclude: List<Long>): List<Long>

    @Query(
        "SELECT id FROM poems WHERE poet = :poet AND collection != 'stories' AND id NOT IN (:exclude) " +
            "AND id NOT IN (10716, 10717, 10724, 10729)"
    )
    suspend fun getCandidateIdsForPoet(poet: String, exclude: List<Long>): List<Long>

    @Query(
        "SELECT id FROM poems WHERE poet = :poet AND collection != 'stories' " +
            "AND id NOT IN (10716, 10717, 10724, 10729)"
    )
    suspend fun getPoemIdsForPoet(poet: String): List<Long>

    @Query("SELECT id FROM poems WHERE poet = :poet AND collection != 'stories' ORDER BY id LIMIT 1 OFFSET :offset")
    suspend fun getPoemIdAtForPoet(poet: String, offset: Int): Long?

    @Query("SELECT COUNT(*) FROM poems WHERE poet = :poet AND collection != 'stories'")
    suspend fun countForPoet(poet: String): Int

    @Query("SELECT COUNT(*) FROM poems WHERE collection = :collection")
    suspend fun countByCollection(collection: String): Int

    @Query("SELECT COUNT(*) FROM poems")
    suspend fun count(): Int

    @Query("SELECT id FROM poems ORDER BY id LIMIT 1 OFFSET :offset")
    suspend fun getPoemIdAt(offset: Int): Long?

    @Query("SELECT COUNT(*) FROM poems")
    fun observeCount(): Flow<Int>

    @Query(
        "SELECT * FROM poems WHERE id IN " +
            "(SELECT rowid FROM poems_fts WHERE poems_fts MATCH :query) ORDER BY number"
    )
    suspend fun search(query: String): List<PoemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoems(poems: List<PoemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFts(rows: List<PoemFtsEntity>)
}

@Dao
interface DrawDao {
    @Insert
    suspend fun insert(draw: DrawRecordEntity): Long

    @Query("SELECT * FROM draws ORDER BY drawnAt DESC")
    fun observeAll(): Flow<List<DrawRecordEntity>>

    @Query("SELECT poemId FROM draws ORDER BY drawnAt DESC LIMIT :limit")
    suspend fun recentPoemIds(limit: Int): List<Long>

    @Query("SELECT COUNT(*) FROM draws WHERE drawnAt >= :since")
    suspend fun countSince(since: Long): Int
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE poemId = :poemId")
    suspend fun delete(poemId: Long)

    @Query("SELECT * FROM favorites WHERE poemId = :poemId")
    fun observe(poemId: Long): Flow<FavoriteEntity?>

    @Query("SELECT * FROM favorites WHERE poemId = :poemId")
    suspend fun get(poemId: Long): FavoriteEntity?

    @Query("SELECT poemId FROM favorites")
    suspend fun favoriteIds(): List<Long>

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>
}


@Dao
interface ReadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markRead(read: ReadEntity)

    @Query("DELETE FROM read_poems WHERE poemId = :poemId")
    suspend fun unmarkRead(poemId: Long)

    @Query("SELECT * FROM read_poems WHERE poemId = :poemId")
    fun observe(poemId: Long): Flow<ReadEntity?>

    @Query("SELECT poemId FROM read_poems")
    fun observeIds(): Flow<List<Long>>
}
