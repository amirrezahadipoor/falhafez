package ir.falhafez.tabir.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** Optional app-wide override for the main reading text color (set from Settings). */
val LocalReadingColor = compositionLocalOf<Color?> { null }

/** Returns the user's chosen reading color, falling back to the theme color. */
@Composable
fun readingColor(fallback: Color): Color = LocalReadingColor.current ?: fallback

/** Font-color presets offered in Settings. */
object FalFontColors {
    const val THEME = "theme"      // follow the active theme
    const val CREAM = "cream"
    const val WHITE = "white"
    const val GOLD = "gold"
    const val EMERALD = "emerald"
    const val AZURE = "azure"

    fun toColor(key: String?): Color? = when (key) {
        CREAM -> Color(0xFFF3E9D2)
        WHITE -> Color(0xFFFFFFFF)
        GOLD -> Color(0xFFE7C878)
        EMERALD -> Color(0xFF7FD8B0)
        AZURE -> Color(0xFFBFD9FF)
        else -> null // THEME / unknown → follow the theme
    }

    fun label(key: String): String = when (key) {
        CREAM -> "کرم"
        WHITE -> "سفید"
        GOLD -> "طلایی"
        EMERALD -> "زمردی"
        AZURE -> "آبی"
        else -> "دنبالهٔ قالب"
    }
}
