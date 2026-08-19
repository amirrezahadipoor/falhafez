package com.amirrezahadipoor.falhafez.presentation.share

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Renders the share image off the main thread, then fires the share intent. */
@Composable
fun SharePoemButton(
    poem: Poem,
    category: FalCategory,
    spec: FalThemeSpec,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    ShareImageRenderer.render(context.applicationContext, poem, category, spec)
                }
                ShareManager.shareImage(context, bitmap)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "اشتراک‌گذاری به‌صورت تصویر",
            tint = tint
        )
    }
}
