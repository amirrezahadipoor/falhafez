package com.amirrezahadipoor.falhafez.core.theme

import androidx.compose.ui.graphics.Color

enum class FalThemeId(val id: String, val faName: String) {
    TAZHIB("tazhib", "تذهیب"),
    CANDLE("candle", "شبِ شمع"),
    GARDEN("garden", "باغِ ستاره"),
    MINIMAL("minimal", "مینیمالِ مدرن");

    companion object {
        fun fromId(id: String?): FalThemeId = entries.firstOrNull { it.id == id } ?: TAZHIB
    }
}

/**
 * A ritual visual theme: background gradient, accents, particles and border tones
 * for the entire draw flow. Data-driven so new themes can be added without touching UI.
 */
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
    val locked: Boolean = false
) {
    companion object {
        val All: List<FalThemeSpec> = listOf(tazhib(), candle(), garden(), minimal())

        fun byId(id: FalThemeId): FalThemeSpec = All.first { it.id == id }

        /** تذهیب — Classic manuscript: deep navy & gold illumination. */
        fun tazhib() = FalThemeSpec(
            id = FalThemeId.TAZHIB,
            backgroundTop = Color(0xFF0B1120),
            backgroundBottom = Color(0xFF101A33),
            accent = Color(0xFFC9A24B),
            accentSoft = Color(0xFFE7C878),
            onBackground = Color(0xFFF3E9D2),
            onBackgroundMuted = Color(0xFFB9A98C),
            card = Color(0xFF131B2E),
            particle = Color(0xFFE7C878),
            particleSecondary = Color(0xFFC9A24B),
            border = Color(0xFF8A6D2F)
        )

        /** شبِ شمع — Candlelight night: warm amber on near-black. */
        fun candle() = FalThemeSpec(
            id = FalThemeId.CANDLE,
            backgroundTop = Color(0xFF1A0F08),
            backgroundBottom = Color(0xFF0A0503),
            accent = Color(0xFFE8A33D),
            accentSoft = Color(0xFFF5C87A),
            onBackground = Color(0xFFF6E7CF),
            onBackgroundMuted = Color(0xFFB99B72),
            card = Color(0xFF241407),
            particle = Color(0xFFE8A33D),
            particleSecondary = Color(0xFFF5C87A),
            border = Color(0xFF7A4E1F)
        )

        /** باغِ ستاره — Starlit garden: deep indigo night, fireflies, teal/violet. */
        fun garden() = FalThemeSpec(
            id = FalThemeId.GARDEN,
            backgroundTop = Color(0xFF0B1030),
            backgroundBottom = Color(0xFF1A1140),
            accent = Color(0xFF6FD3C7),
            accentSoft = Color(0xFFA8E6DE),
            onBackground = Color(0xFFE9F2F5),
            onBackgroundMuted = Color(0xFF9BB0BF),
            card = Color(0xFF121838),
            particle = Color(0xFFBFE8FF),
            particleSecondary = Color(0xFF6FD3C7),
            border = Color(0xFF35506B)
        )

        /** مینیمالِ مدرن — Modern minimal: calm cream & terracotta. */
        fun minimal() = FalThemeSpec(
            id = FalThemeId.MINIMAL,
            backgroundTop = Color(0xFFF7EFE4),
            backgroundBottom = Color(0xFFEFE4D3),
            accent = Color(0xFFB65C3A),
            accentSoft = Color(0xFFD98A5F),
            onBackground = Color(0xFF2B2118),
            onBackgroundMuted = Color(0xFF6E5C4A),
            card = Color(0xFFFFFFFF),
            particle = Color(0xFFD9C6A5),
            particleSecondary = Color(0xFFC4AC85),
            border = Color(0xFFD8C9B0)
        )
    }
}
