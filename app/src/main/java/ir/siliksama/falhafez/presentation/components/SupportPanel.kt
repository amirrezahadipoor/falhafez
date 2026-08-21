package ir.siliksama.falhafez.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.PersianText
import ir.siliksama.falhafez.domain.model.SupportTier

private fun formatPrice(toman: Int): String =
    PersianText.digits(toman.toString().reversed().chunked(3).joinToString(",").reversed())

/** سه سطح حمایت مالی — مشترک بین تنظیمات و دیالوگِ قلبِ صفحهٔ اصلی. */
@Composable
fun SupportPanel(
    currentTier: SupportTier,
    purchasing: Boolean,
    onPurchase: (SupportTier) -> Unit,
    spec: FalThemeSpec,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (currentTier != SupportTier.NONE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(spec.accent.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text("وضعیت: ${currentTier.faName} ✓", style = FalText.body, color = spec.accentSoft)
                Text("تبلیغات برای همیشه حذف شده است. سپاس از حمایت شما ♥", style = FalText.caption, color = spec.onBackgroundMuted)
            }
        }

        listOf(SupportTier.BASE, SupportTier.PLUS, SupportTier.GOLD).forEach { tier ->
            val active = currentTier == tier
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (active) spec.accent.copy(alpha = 0.14f) else spec.card, RoundedCornerShape(16.dp))
                    .border(1.5.dp, if (active) spec.accent else spec.border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tier.faName, style = FalText.heading, color = spec.accentSoft, modifier = Modifier.weight(1f))
                    Text("${formatPrice(tier.priceToman)} تومان", style = FalText.heading, color = spec.onBackground)
                }
                Text(tier.perks, style = FalText.caption, color = spec.onBackgroundMuted)
                TextButton(
                    onClick = { if (!active) onPurchase(tier) },
                    enabled = !active && !purchasing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (active) spec.accent.copy(alpha = 0.30f) else spec.accent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = when {
                            active -> "فعال ✓"
                            purchasing -> "در حال پرداخت…"
                            else -> "حمایت و حذفِ تبلیغات"
                        },
                        style = FalText.button,
                        color = if (active) spec.onBackground else Color(0xFF14100A)
                    )
                }
            }
        }
    }
}
