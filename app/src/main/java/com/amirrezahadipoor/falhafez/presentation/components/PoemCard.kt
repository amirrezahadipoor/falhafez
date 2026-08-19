package com.amirrezahadipoor.falhafez.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.domain.model.Poem

/** Ornamental card used across the library, history and favorites lists. */
@Composable
fun PoemCard(
    poem: Poem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(18.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = "${poem.collection.poet.faName} — ${poem.collection.faName}",
            style = FalText.caption,
            color = FalPalette.CreamMuted
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = poem.opening,
            style = FalText.verseSmall,
            color = FalPalette.Cream,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
