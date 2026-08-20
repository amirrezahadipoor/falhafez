package ir.falhafez.tabir.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.falhafez.tabir.core.designsystem.FalPalette
import ir.falhafez.tabir.core.designsystem.FalText
import ir.falhafez.tabir.core.util.PersianText
import ir.falhafez.tabir.domain.model.DrawEntry

/** کارنامهٔ شخصی فال — keeps the user coming back by turning draws into a story. */
@Composable
fun StatsView(history: List<DrawEntry>, modifier: Modifier = Modifier) {
    if (history.isEmpty()) return

    val total = history.size
    val dayNumbers = history.map { it.drawnAt / 86_400_000L }.distinct().sortedDescending()
    val today = System.currentTimeMillis() / 86_400_000L

    // current streak (consecutive days up to today/yesterday)
    var streak = 0
    var expected = today
    if (dayNumbers.firstOrNull() == today || dayNumbers.firstOrNull() == today - 1) {
        expected = dayNumbers.first()
        for (d in dayNumbers) {
            if (d == expected) {
                streak++
                expected -= 1
            } else if (d < expected) break
        }
    }

    val bestStreak = run {
        var best = 0
        var run = 0
        var prev: Long? = null
        for (d in dayNumbers.sorted()) {
            run = if (prev != null && d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        best
    }

    val byPoet = history.groupingBy { it.poem.poet.faName }.eachCount()
    val byTheme = history.groupingBy { it.poem.themeTag }.eachCount()
    val byHour = history.groupingBy { java.util.Calendar.getInstance().apply { timeInMillis = it.drawnAt }.get(java.util.Calendar.HOUR_OF_DAY) }.eachCount()
    val topPoet = byPoet.maxByOrNull { it.value }?.key ?: "—"
    val topTheme = byTheme.maxByOrNull { it.value }?.key ?: "—"
    val topHour = byHour.maxByOrNull { it.value }?.key ?: -1
    val topHourFa = if (topHour in 0..23) PersianText.number(topHour) else "—"

    val themeFa = mapOf(
        "love" to "عشق", "hope" to "امید", "patience" to "صبوری", "joy" to "شادی",
        "new-beginnings" to "آغاز تازه", "travel" to "سفر", "effort" to "کوشش",
        "wisdom" to "حکمت", "compassion" to "مهرورزی", "legacy" to "ماندگاری",
        "faith" to "ایمان", "decision" to "تصمیم", "general" to "عمومی"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatRow("فال‌های گرفته‌شده", PersianText.number(total))
        StatRow("زنجیرهٔ فعلی", "${PersianText.number(streak)} روز")
        StatRow("بهترین زنجیره", "${PersianText.number(bestStreak)} روز")
        StatRow("شاعرِ غالب", topPoet)
        StatRow("موضوعِ غالب", themeFa[topTheme] ?: topTheme)
        StatRow("ساعتِ پرفال", topHourFa)
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FalPalette.NavySoft, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = FalText.bodyMuted, color = FalPalette.CreamMuted)
        Text(value, style = FalText.heading, color = FalPalette.GoldBright)
    }
}
