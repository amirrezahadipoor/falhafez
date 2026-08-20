package ir.siliksama.falhafez.core.util

import java.util.Calendar

/** Minimal, dependency-free Gregorian → Jalali (Persian) calendar conversion. */
object Jalali {

    private val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    data class Date(val year: Int, val month: Int, val day: Int)

    fun fromGregorian(gy: Int, gm: Int, gd: Int): Date {
        val gDM = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gDM[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Date(jy, jm, jd)
    }

    fun format(epochMillis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val j = fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val hour = PersianText.digits(c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0'))
        val minute = PersianText.digits(c.get(Calendar.MINUTE).toString().padStart(2, '0'))
        return "${PersianText.number(j.day)} ${months[j.month - 1]} ${PersianText.number(j.year)} — ساعت $hour:$minute"
    }

    fun shortDate(epochMillis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val j = fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        return "${PersianText.number(j.day)} ${months[j.month - 1]} ${PersianText.number(j.year)}"
    }
}
