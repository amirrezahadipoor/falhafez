package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.usecase.DailyFalUseCase.Companion.indexForDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * فالِ روز باید **قطعی** باشد (همه یک فال ببینند، ویجت با اپ یکی باشد)
 * ولی **قابلِ پیش‌بینی نباشد** (فردا غزلِ بعدی نباشد).
 */
class DailyFalTest {

    private val count = 495   // غزلیاتِ حافظ

    @Test
    fun `same day always gives the same poem`() {
        repeat(50) { d ->
            val day = 20_000L + d
            assertEquals(indexForDay(day, count), indexForDay(day, count))
        }
    }

    @Test
    fun `index is always inside the collection`() {
        for (d in 0 until 5_000) {
            val i = indexForDay(d.toLong(), count)
            assertTrue("اندیس $i خارج از محدوده است", i in 0 until count)
        }
    }

    @Test
    fun `consecutive days are not sequential`() {
        // باگِ قبلی: day % count ⇒ فردا همیشه غزلِ بعدی.
        var sequential = 0
        for (d in 0 until 1_000) {
            val a = indexForDay(d.toLong(), count)
            val b = indexForDay(d + 1L, count)
            if (b == (a + 1) % count) sequential++
        }
        assertTrue("الگوی ترتیبی دیده شد ($sequential بار)", sequential < 20)
    }

    @Test
    fun `distribution covers the divan reasonably`() {
        // در دو سال، باید بخشِ بزرگی از دیوان دیده شود.
        val seen = (0 until 730).map { indexForDay(it.toLong(), count) }.toSet()
        assertTrue("پوششِ ضعیف: ${seen.size} غزل از $count", seen.size > count / 2)
    }

    @Test
    fun `never negative even for large day numbers`() {
        listOf(Long.MAX_VALUE, 1L shl 40, 999_999_999L).forEach {
            assertTrue(indexForDay(it, count) >= 0)
        }
    }
}
