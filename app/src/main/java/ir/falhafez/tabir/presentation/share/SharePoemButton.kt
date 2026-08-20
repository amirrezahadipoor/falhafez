package ir.falhafez.tabir.presentation.share

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ir.falhafez.tabir.core.theme.FalThemeSpec
import ir.falhafez.tabir.domain.model.FalCategory
import ir.falhafez.tabir.domain.model.Poem

/** Opens the share sheet (Telegram / WhatsApp / Rubika / Bale / Instagram / …). */
@Composable
fun SharePoemButton(
    poem: Poem,
    category: FalCategory,
    spec: FalThemeSpec,
    tint: Color,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }

    IconButton(onClick = { showSheet = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "اشتراک‌گذاری به‌صورت تصویر",
            tint = tint
        )
    }

    if (showSheet) {
        ShareSheet(
            poem = poem,
            category = category,
            spec = spec,
            onDismiss = { showSheet = false }
        )
    }
}
