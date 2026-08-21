package ir.siliksama.falhafez.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.FtsOptions

@Entity(
    tableName = "poems",
    indices = [Index("poet"), Index("collection")]
)
data class PoemEntity(
    @PrimaryKey val id: Long,
    val poet: String,
    val collection: String,
    val number: Int,
    val themeTag: String,
    val tafsir: String
)

@Entity(
    tableName = "verses",
    foreignKeys = [
        ForeignKey(
            entity = PoemEntity::class,
            parentColumns = ["id"],
            childColumns = ["poemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("poemId")]
)
data class VerseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poemId: Long,
    val position: Int,
    val first: String,
    val second: String?,
    val meaning: String? = null
)

/**
 * Standalone FTS4 table (kept in sync by the seeder — the corpus is immutable after seed).
 * Persian-friendly unicode61 tokenizer.
 */
@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "poems_fts")
data class PoemFtsEntity(
    @ColumnInfo(name = "rowid")
    @PrimaryKey val poemId: Long,
    val text: String
)

@Entity(tableName = "draws")
data class DrawRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poemId: Long,
    val question: String?,
    val category: String?,
    val drawnAt: Long
)

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["poemId"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poemId: Long,
    val createdAt: Long
)

data class PoemWithVerses(
    @Embedded val poem: PoemEntity,
    @Relation(parentColumn = "id", entityColumn = "poemId")
    val verses: List<VerseEntity>
)


@Entity(tableName = "read_poems", indices = [Index(value = ["poemId"], unique = true)])
data class ReadEntity(
    @PrimaryKey val poemId: Long,
    val createdAt: Long
)
