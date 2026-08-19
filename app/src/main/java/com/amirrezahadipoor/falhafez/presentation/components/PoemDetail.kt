package com.amirrezahadipoor.falhafez.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import com.amirrezahadipoor.falhafez.domain.model.Verse
import com.amirrezahadipoor.falhafez.presentation.share.SharePoemButton

/** Full-poem reader shared by Library / History / Favorites. */
@Composable
fun PoemDetail(
    poem: Poem,
    category: FalCategory,
    spec: FalThemeSpec,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            title = "${poem.collection.poet.faName} — ${poem.collection.faName}",
            onBack = onBack,
            titleColor = spec.onBackground
        )

        Spacer(Modifier.height(8.dp))
        poem.verses.forEach { verse ->
            Text(verse.first, style = FalText.verse, color = spec.onBackground)
            if (verse.isCouplet) {
                Spacer(Modifier.height(4.dp))
                Text(verse.second!!, style = FalText.verse, color = spec.onBackground)
            }
            Spacer(Modifier.height(16.dp))
        }

        OrnamentalDivider(color = spec.accent, modifier = Modifier.fillMaxWidth(0.7f))
        Spacer(Modifier.height(18.dp))

        Text("تفسیر", style = FalText.heading, color = spec.accent)
        Spacer(Modifier.height(10.dp))
        Text(poem.tafsir, style = FalText.tafsir, color = spec.onBackground)

        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "علاقه‌مندی",
                    tint = if (isFavorite) spec.accent else spec.onBackgroundMuted
                )
            }
            SharePoemButton(
                poem = poem,
                category = category,
                spec = spec,
                tint = spec.onBackgroundMuted
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}
