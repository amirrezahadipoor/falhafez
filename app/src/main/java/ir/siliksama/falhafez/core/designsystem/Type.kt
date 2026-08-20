package ir.siliksama.falhafez.core.designsystem

import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.siliksama.falhafez.R

/** Reading / UI face — Vazirmatn (OFL). */
val Vazir = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold)
)

/**
 * Calligraphy / display face — Noto Nastaliq Urdu (OFL).
 * The bundled file is a variable font, which Android only supports from API 26,
 * so we fall back to a bold Vazirmatn on older devices (API 23–25).
 */
val Nastaliq: FontFamily = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(Font(R.font.noto_nastaliq_urdu, FontWeight.Normal))
} else {
    FontFamily(Font(R.font.vazirmatn_bold, FontWeight.Bold))
}

/** Named text styles used across the app (colors are set at call sites). */
object FalText {
    val display = TextStyle(fontFamily = Nastaliq, fontWeight = FontWeight.Normal, fontSize = 46.sp, lineHeight = 92.sp)
    val displaySmall = TextStyle(fontFamily = Nastaliq, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 68.sp)
    val verse = TextStyle(fontFamily = Nastaliq, fontWeight = FontWeight.Normal, fontSize = 23.sp, lineHeight = 56.sp)
    val verseSmall = TextStyle(fontFamily = Nastaliq, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 48.sp)
    val title = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 40.sp)
    val heading = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 30.sp)
    val body = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 28.sp)
    val bodyMuted = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 24.sp)
    val tafsir = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 32.sp)
    val button = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
    val caption = TextStyle(fontFamily = Vazir, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 20.sp)
}

val FalTypography = Typography(
    displayLarge = FalText.display,
    displayMedium = FalText.displaySmall,
    headlineLarge = FalText.title,
    headlineMedium = FalText.heading,
    titleLarge = FalText.heading,
    titleMedium = FalText.body,
    bodyLarge = FalText.body,
    bodyMedium = FalText.bodyMuted,
    bodySmall = FalText.caption,
    labelLarge = FalText.button,
    labelMedium = FalText.caption,
    labelSmall = FalText.caption
)
