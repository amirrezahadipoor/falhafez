package ir.siliksama.falhafez.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = FalPalette.Gold,
    onPrimary = FalPalette.Navy,
    primaryContainer = FalPalette.NavyLight,
    onPrimaryContainer = FalPalette.GoldBright,
    secondary = FalPalette.GoldDeep,
    onSecondary = FalPalette.Cream,
    secondaryContainer = FalPalette.NavyLight,
    onSecondaryContainer = FalPalette.Cream,
    tertiary = FalPalette.Teal,
    background = FalPalette.Navy,
    onBackground = FalPalette.Cream,
    surface = FalPalette.NavySoft,
    onSurface = FalPalette.Cream,
    surfaceVariant = FalPalette.NavyLight,
    onSurfaceVariant = FalPalette.CreamMuted,
    outline = FalPalette.GoldDeep,
    error = FalPalette.Error
)

/**
 * App-wide Material theme. The ritual draw flow applies a per-theme [FalThemeSpec]
 * on top of this for its backgrounds and accents.
 */
@Composable
fun FalHafezTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = FalTypography,
        shapes = FalShapes,
        content = content
    )
}
