package ir.siliksama.falhafez.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.siliksama.falhafez.core.designsystem.FalText
import ir.siliksama.falhafez.core.theme.FalThemeSpec
import ir.siliksama.falhafez.core.util.DayNumber
import ir.siliksama.falhafez.core.util.FalStats
import ir.siliksama.falhafez.core.util.PersianText
import ir.siliksama.falhafez.domain.model.DrawEntry

/** کارنامهٔ شخصی فال — keeps the user coming back by turning draws into a story. */
@Composable
fun StatsView(history: List<DrawEntry>, spec: FalThemeSpec, modifier: Modifier = Modifier) {
    if (history.isEmpty()) return

    val total = history.size
    // روزِ محلی — تا زنجیرهٔ روزانه با «فالِ روز» و ویجت هم‌مرز باشد (نه نیمه‌شبِ UTC).
    val dayNumbers = history.map { DayNumber.local(it.drawnAt) }.distinct().sortedDescending()
    val today = DayNumber.local()

    val streak = FalStats.currentStreak(dayNumbers, today)
    val bestStreak = FalStats.bestStreak(dayNumbers)

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
        StatRow("فال‌های گرفته‌شده", PersianText.number(total), spec)
        StatRow("زنجیرهٔ فعلی", "${PersianText.number(streak)} روز", spec)
        StatRow("بهترین زنجیره", "${PersianText.number(bestStreak)} روز", spec)
        StatRow("شاعرِ غالب", topPoet, spec)
        StatRow("موضوعِ غالب", themeFa[topTheme] ?: topTheme, spec)
        StatRow("ساعتِ پرفال", topHourFa, spec)
    }
}

@Composable
private fun StatRow(label: String, value: String, spec: FalThemeSpec) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(spec.card, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = FalText.bodyMuted, color = spec.onBackgroundMuted)
        Text(value, style = FalText.heading, color = spec.accentSoft)
    }
}
