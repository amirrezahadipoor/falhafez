package ir.siliksama.falhafez.domain.model

enum class Poet(val key: String, val faName: String) {
    HAFEZ("hafez", "حافظ"),
    SAADI("saadi", "سعدی"),
    RUMI("rumi", "مولانا"),
    KHAYYAM("khayyam", "خیام");

    companion object {
        fun fromKey(key: String): Poet = entries.firstOrNull { it.key == key } ?: HAFEZ
    }
}

enum class Collection(val key: String, val faName: String, val poet: Poet) {
    HAFEZ_GHAZAL("ghazal", "غزلیات", Poet.HAFEZ),
    HAFEZ_GHETE("qete", "قطعات", Poet.HAFEZ),
    HAFEZ_ROBAEE("hafez-robaee", "رباعیات", Poet.HAFEZ),
    HAFEZ_QASIDE("qaside", "قصاید", Poet.HAFEZ),
    HAFEZ_ATTRIBUTED("attributed", "اشعار منتسب", Poet.HAFEZ),
    SAADI_GOLESTAN("golestan", "گلستان", Poet.SAADI),
    SAADI_BUSTAN("bustan", "بوستان", Poet.SAADI),
    SAADI_GHAZAL("saadi-ghazal", "غزلیات", Poet.SAADI),
    SAADI_ROBAEE("saadi-robaee", "رباعیات", Poet.SAADI),
    SAADI_GHETE("saadi-ghete", "قطعات", Poet.SAADI),
    SAADI_MOLHAGHAT("saadi-molhaghat", "ملحقات", Poet.SAADI),
    STORIES("stories", "جهان", Poet.SAADI),
    RUMI_MASNAVI("masnavi", "مثنوی معنوی", Poet.RUMI),
    RUMI_SHAMS("shams", "دیوان شمس", Poet.RUMI),
    RUMI_ROBAEE("robaee", "رباعیات", Poet.RUMI),
    KHAYYAM_RUBAI("rubaiyat", "رباعیات", Poet.KHAYYAM);

    companion object {
        fun fromKey(key: String): Collection? = entries.firstOrNull { it.key == key }
        fun byPoet(poet: Poet): List<Collection> = entries.filter { it.poet == poet }
    }
}

enum class FalCategory(val key: String, val faName: String) {
    NONE("none", "عمومی"),
    LOVE("love", "عشق"),
    CAREER("career", "کار و پیشه"),
    TRAVEL("travel", "سفر"),
    HEALTH("health", "سلامتی"),
    DECISION("decision", "تصمیم");

    companion object {
        fun fromKey(key: String?): FalCategory = entries.firstOrNull { it.key == key } ?: NONE
    }
}

data class Verse(
    val position: Int,
    val first: String,
    val second: String?,
    val meaning: String? = null
) {
    val isCouplet: Boolean get() = !second.isNullOrBlank()
    val fullText: String get() = if (isCouplet) "$first؛ $second" else first
}

data class Poem(
    val id: Long,
    val poet: Poet,
    val collection: Collection,
    val number: Int,
    val themeTag: String,
    val tafsir: String,
    val verses: List<Verse>
) {
    val opening: String
        get() = verses.firstOrNull()?.fullText ?: ""

    val title: String
        get() = if (number > 0) "${collection.faName} — ${PersianOrdinal.number(number)}" else collection.faName
}

object PersianOrdinal {
    private val ordinals = listOf(
        "اول", "دوم", "سوم", "چهارم", "پنجم", "ششم", "هفتم", "هشتم", "نهم", "دهم",
        "یازدهم", "دوازدهم", "سیزدهم", "چهاردهم", "پانزدهم", "شانزدهم", "هفدهم", "هجدهم", "نوزدهم", "بیستم"
    )
    fun number(n: Int): String = if (n in 1..ordinals.size) ordinals[n - 1] else ir.siliksama.falhafez.core.util.PersianText.number(n)
}

data class DrawEntry(
    val id: Long,
    val poem: Poem,
    val question: String?,
    val category: FalCategory,
    val drawnAt: Long
)
