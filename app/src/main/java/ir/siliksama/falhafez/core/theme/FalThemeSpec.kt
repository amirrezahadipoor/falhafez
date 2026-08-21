package ir.siliksama.falhafez.core.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import ir.siliksama.falhafez.R

enum class FalThemeId(val id: String, val faName: String) {
    TAZHIB("tazhib", "تذهیب"),
    CANDLE("candle", "شبِ شمع"),
    GARDEN("garden", "باغِ ستاره"),
    MINIMAL("minimal", "مینیمالِ مدرن"),
    NOWRUZ("nowruz", "نوروز"),
    YALDA("yalda", "شبِ یلدا"),
    DAWN("dawn", "سپیده‌دم"),
    SEA("sea", "شبِ دریا"),
    DESERT("desert", "کویر"),
    MOONLIGHT("moonlight", "مهتاب"),
    ROSE("rose", "گل و مرغ"),
    MORNING("morning", "صبحِ روشن"),
    PARCHMENT("parchment", "کاغذِ کهنه");

    companion object {
        fun fromId(id: String?): FalThemeId = entries.firstOrNull { it.id == id } ?: TAZHIB
    }
}

data class FalThemeSpec(
    val id: FalThemeId,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val accent: Color,
    val accentSoft: Color,
    val onBackground: Color,
    val onBackgroundMuted: Color,
    val card: Color,
    val particle: Color,
    val particleSecondary: Color,
    val border: Color,
    @DrawableRes val artworkRes: Int? = null,
    val artworkAlpha: Float = 0.55f
) {
    companion object {
        val All: List<FalThemeSpec> = listOf(
            tazhib(), candle(), garden(), minimal(), nowruz(), yalda(),
            dawn(), sea(), desert(), moonlight(), rose(), morning(), parchment()
        )

        fun byId(id: FalThemeId): FalThemeSpec = All.first { it.id == id }

        fun tazhib() = FalThemeSpec(
            id = FalThemeId.TAZHIB,
            backgroundTop = Color(0xFF0B1120), backgroundBottom = Color(0xFF101A33),
            accent = Color(0xFFC9A24B), accentSoft = Color(0xFFE7C878),
            onBackground = Color(0xFFF3E9D2), onBackgroundMuted = Color(0xFFB9A98C),
            card = Color(0xFF131B2E), particle = Color(0xFFE7C878), particleSecondary = Color(0xFFC9A24B),
            border = Color(0xFF8A6D2F), artworkRes = R.drawable.theme_tazhib, artworkAlpha = 0.60f
        )

        fun candle() = FalThemeSpec(
            id = FalThemeId.CANDLE,
            backgroundTop = Color(0xFF1A0F08), backgroundBottom = Color(0xFF0A0503),
            accent = Color(0xFFE8A33D), accentSoft = Color(0xFFF5C87A),
            onBackground = Color(0xFFF6E7CF), onBackgroundMuted = Color(0xFFB99B72),
            card = Color(0xFF241407), particle = Color(0xFFE8A33D), particleSecondary = Color(0xFFF5C87A),
            border = Color(0xFF7A4E1F), artworkRes = R.drawable.theme_candle, artworkAlpha = 0.60f
        )

        fun garden() = FalThemeSpec(
            id = FalThemeId.GARDEN,
            backgroundTop = Color(0xFF0B1030), backgroundBottom = Color(0xFF1A1140),
            accent = Color(0xFF6FD3C7), accentSoft = Color(0xFFA8E6DE),
            onBackground = Color(0xFFE9F2F5), onBackgroundMuted = Color(0xFF9BB0BF),
            card = Color(0xFF121838), particle = Color(0xFFBFE8FF), particleSecondary = Color(0xFF6FD3C7),
            border = Color(0xFF35506B), artworkRes = R.drawable.theme_garden, artworkAlpha = 0.60f
        )

        fun minimal() = FalThemeSpec(
            id = FalThemeId.MINIMAL,
            backgroundTop = Color(0xFFF7EFE4), backgroundBottom = Color(0xFFEFE4D3),
            accent = Color(0xFFB65C3A), accentSoft = Color(0xFFD98A5F),
            onBackground = Color(0xFF2B2118), onBackgroundMuted = Color(0xFF6E5C4A),
            card = Color(0xFFFFFFFF), particle = Color(0xFFD9C6A5), particleSecondary = Color(0xFFC4AC85),
            border = Color(0xFFD8C9B0), artworkRes = R.drawable.theme_minimal, artworkAlpha = 0.80f
        )

        fun nowruz() = FalThemeSpec(
            id = FalThemeId.NOWRUZ,
            backgroundTop = Color(0xFF0E2417), backgroundBottom = Color(0xFF0A1A10),
            accent = Color(0xFF7AC74F), accentSoft = Color(0xFFC6E9A6),
            onBackground = Color(0xFFF2F7EC), onBackgroundMuted = Color(0xFFA9C4A0),
            card = Color(0xFF12291B), particle = Color(0xFFA8E063), particleSecondary = Color(0xFF7AC74F),
            border = Color(0xFF3E6B3A), artworkRes = R.drawable.theme_nowruz, artworkAlpha = 0.55f
        )

        fun yalda() = FalThemeSpec(
            id = FalThemeId.YALDA,
            backgroundTop = Color(0xFF2A0F14), backgroundBottom = Color(0xFF140609),
            accent = Color(0xFFE05263), accentSoft = Color(0xFFF18A97),
            onBackground = Color(0xFFFBE9E4), onBackgroundMuted = Color(0xFFC79A9F),
            card = Color(0xFF33151B), particle = Color(0xFFF27E8E), particleSecondary = Color(0xFFE05263),
            border = Color(0xFF7A2A33), artworkRes = R.drawable.theme_yalda, artworkAlpha = 0.60f
        )

        fun dawn() = FalThemeSpec(
            id = FalThemeId.DAWN,
            backgroundTop = Color(0xFF241725), backgroundBottom = Color(0xFF4A2A3A),
            accent = Color(0xFFE8A0B4), accentSoft = Color(0xFFF2C6D0),
            onBackground = Color(0xFFFBECEF), onBackgroundMuted = Color(0xFFC9A0AB),
            card = Color(0xFF2C1C24), particle = Color(0xFFF5C8D2), particleSecondary = Color(0xFFE8A0B4),
            border = Color(0xFF7A4A58), artworkRes = R.drawable.theme_dawn, artworkAlpha = 0.55f
        )

        fun sea() = FalThemeSpec(
            id = FalThemeId.SEA,
            backgroundTop = Color(0xFF06202B), backgroundBottom = Color(0xFF0A3640),
            accent = Color(0xFF3FB8AF), accentSoft = Color(0xFF8FE0D8),
            onBackground = Color(0xFFE8F6F4), onBackgroundMuted = Color(0xFF9EC2BE),
            card = Color(0xFF0B2A33), particle = Color(0xFF9FE8E0), particleSecondary = Color(0xFF5FD4C8),
            border = Color(0xFF1E5A5A), artworkRes = R.drawable.theme_sea, artworkAlpha = 0.55f
        )

        fun desert() = FalThemeSpec(
            id = FalThemeId.DESERT,
            backgroundTop = Color(0xFF1A1208), backgroundBottom = Color(0xFF0E0A05),
            accent = Color(0xFFD9A24B), accentSoft = Color(0xFFF0C98A),
            onBackground = Color(0xFFF6EAD5), onBackgroundMuted = Color(0xFFC0A47A),
            card = Color(0xFF221708), particle = Color(0xFFF0C98A), particleSecondary = Color(0xFFD9A24B),
            border = Color(0xFF7A5A2A), artworkRes = R.drawable.theme_desert, artworkAlpha = 0.55f
        )

        fun moonlight() = FalThemeSpec(
            id = FalThemeId.MOONLIGHT,
            backgroundTop = Color(0xFF0B1024), backgroundBottom = Color(0xFF131A3A),
            accent = Color(0xFF9FB8E8), accentSoft = Color(0xFFC9D6F5),
            onBackground = Color(0xFFE9EEFA), onBackgroundMuted = Color(0xFF9FA9C4),
            card = Color(0xFF10162E), particle = Color(0xFFC9D6F5), particleSecondary = Color(0xFF9FB8E8),
            border = Color(0xFF3A4A6B), artworkRes = R.drawable.theme_moonlight, artworkAlpha = 0.55f
        )

        /** گل و مرغ — light ivory & rose (روشن). */
        fun rose() = FalThemeSpec(
            id = FalThemeId.ROSE,
            backgroundTop = Color(0xFFFDF6F0), backgroundBottom = Color(0xFFF7E7E2),
            accent = Color(0xFFC25B6E), accentSoft = Color(0xFFE08B9A),
            onBackground = Color(0xFF3A2228), onBackgroundMuted = Color(0xFF8A626C),
            card = Color(0xFFFFFFFF), particle = Color(0xFFE8A8B4), particleSecondary = Color(0xFFC25B6E),
            border = Color(0xFFD9A6AE), artworkRes = R.drawable.theme_rose, artworkAlpha = 0.72f
        )

        /** صبحِ روشن — bright morning sky (روشن). */
        fun morning() = FalThemeSpec(
            id = FalThemeId.MORNING,
            backgroundTop = Color(0xFFF2F9FF), backgroundBottom = Color(0xFFE3F0FA),
            accent = Color(0xFF2E7DB8), accentSoft = Color(0xFF63A7D8),
            onBackground = Color(0xFF1C2A38), onBackgroundMuted = Color(0xFF5A7286),
            card = Color(0xFFFFFFFF), particle = Color(0xFFA9D4F2), particleSecondary = Color(0xFF6FB2E6),
            border = Color(0xFFB6CFE2), artworkRes = R.drawable.theme_morning, artworkAlpha = 0.72f
        )

        /** کاغذِ کهنه — aged manuscript paper (روشن). */
        fun parchment() = FalThemeSpec(
            id = FalThemeId.PARCHMENT,
            backgroundTop = Color(0xFFF6EBD6), backgroundBottom = Color(0xFFE9D9BC),
            accent = Color(0xFF9A7B3C), accentSoft = Color(0xFFC0A163),
            onBackground = Color(0xFF332712), onBackgroundMuted = Color(0xFF7A6640),
            card = Color(0xFFFFFBF2), particle = Color(0xFFD9C193), particleSecondary = Color(0xFFC0A163),
            border = Color(0xFFCDB88E), artworkRes = R.drawable.theme_parchment, artworkAlpha = 0.75f
        )
    }
}
