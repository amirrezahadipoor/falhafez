package ir.siliksama.falhafez.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.FalPalette
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.domain.model.Poem

/** Compact ornamental card (2-column grid tile) used across library, search, favorites. */
@Composable
fun PoemCard(
    poem: Poem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRead: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isRead) FalPalette.GoldDeep.copy(alpha = 0.7f) else FalPalette.GoldDeep.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${poem.collection.poet.faName} — ${poem.collection.faName}",
                style = FalText.caption,
                color = FalPalette.CreamMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isRead) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "خوانده‌شده",
                    tint = FalPalette.Gold,
                    modifier = Modifier.height(14.dp)
                )
            }
        }
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
