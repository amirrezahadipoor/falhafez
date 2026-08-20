package ir.falhafez.tabir.presentation.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.falhafez.tabir.core.designsystem.FalPalette
import ir.falhafez.tabir.core.designsystem.FalText
import ir.falhafez.tabir.domain.model.Poem

/** Compact ornamental card (2-column grid tile) used across library, search, favorites. */
@Composable
fun PoemCard(
    poem: Poem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
            .border(1.dp, FalPalette.GoldDeep.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = "${poem.collection.poet.faName} — ${poem.collection.faName}",
            style = FalText.caption,
            color = FalPalette.CreamMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = poem.opening,
            style = FalText.verseSmall,
            color = FalPalette.Cream,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
