package ir.siliksama.falhafez

import ir.siliksama.falhafez.core.util.Jalali
import org.junit.Assert.assertEquals
import org.junit.Test

/** تست واحد تبدیل میلادی → جلالی با نقاط مرجع معروف. */
class JalaliTest {

    @Test
    fun nowruz2024() {
        val j = Jalali.fromGregorian(2024, 3, 20)
        assertEquals(1403, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun nowruz2023() {
        val j = Jalali.fromGregorian(2023, 3, 21)
        assertEquals(1402, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun nowruz2025() {
        val j = Jalali.fromGregorian(2025, 3, 21)
        assertEquals(1404, j.year)
        assertEquals(1, j.month)
        assertEquals(1, j.day)
    }

    @Test
    fun islamicRevolution() {
        val j = Jalali.fromGregorian(1979, 2, 11)
        assertEquals(1357, j.year)
        assertEquals(11, j.month)
        assertEquals(22, j.day)
    }
}
