package ir.siliksama.falhafez

import ir.siliksama.falhafez.core.util.DayNumber
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * قفلِ رگرسیون برای باگِ «ناهماهنگی فالِ روزِ اپ و ویجت»:
 * شمارهٔ روز باید بر پایهٔ نیمه‌شبِ محلی (تهران) باشد، نه UTC.
 */
class DayNumberTest {

    private var previous: TimeZone? = null

    @Before
    fun setTehranTimeZone() {
        previous = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tehran"))
    }

    @After
    fun restoreTimeZone() {
        previous?.let { TimeZone.setDefault(it) }
    }

    @Test
    fun localDayDiffersFromUtcDayForEvening() {
        // ۲۳:۳۰ تهران = ۲۰:۰۰ UTC همان روز؛ ولی نیمه‌شبِ محلی در روزِ UTC قبل است.
        val evening = GregorianCalendar(2026, Calendar.AUGUST, 20, 23, 30).timeInMillis
        val utcDay = evening / 86_400_000L
        val localDay = DayNumber.local(evening)
        assertEquals(utcDay - 1, localDay)
    }

    @Test
    fun consecutiveLocalDaysDifferByOne() {
        val lateNight = GregorianCalendar(2026, Calendar.AUGUST, 20, 23, 30).timeInMillis
        val earlyMorning = GregorianCalendar(2026, Calendar.AUGUST, 21, 0, 30).timeInMillis
        assertEquals(DayNumber.local(lateNight) + 1, DayNumber.local(earlyMorning))
    }

    @Test
    fun sameLocalDayStableAcrossHours() {
        val morning = GregorianCalendar(2026, Calendar.AUGUST, 21, 1, 0).timeInMillis
        val night = GregorianCalendar(2026, Calendar.AUGUST, 21, 23, 0).timeInMillis
        assertEquals(DayNumber.local(morning), DayNumber.local(night))
    }
}
