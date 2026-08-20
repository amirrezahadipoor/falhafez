package ir.falhafez.tabir.data.local.seed

import ir.falhafez.tabir.data.local.PoemEntity
import ir.falhafez.tabir.data.local.PoemFtsEntity
import ir.falhafez.tabir.data.local.VerseEntity
import kotlinx.serialization.Serializable

@Serializable
data class SeedVerse(
    val first: String,
    val second: String? = null,
    val meaning: String? = null
)

@Serializable
data class SeedPoem(
    val id: Long,
    val poet: String,
    val collection: String,
    val number: Int = 0,
    val themeTag: String = "general",
    val tafsir: String = "",
    val verses: List<SeedVerse>
) {
    fun toEntity(): PoemEntity = PoemEntity(
        id = id,
        poet = poet,
        collection = collection,
        number = number,
        themeTag = themeTag,
        tafsir = tafsir
    )

    fun toVerses(): List<VerseEntity> = verses.mapIndexed { index, verse ->
        VerseEntity(
            poemId = id, position = index + 1,
            first = verse.first, second = verse.second, meaning = verse.meaning
        )
    }

    fun toFts(): PoemFtsEntity = PoemFtsEntity(
        poemId = id,
        text = verses.joinToString(" ") { (it.first ?: "") + " " + (it.second ?: "") }
    )
}
