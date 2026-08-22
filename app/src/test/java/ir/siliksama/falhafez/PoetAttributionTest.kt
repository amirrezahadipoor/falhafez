package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * درستیِ انتسابِ آثار.
 *
 * پیش‌تر ۵۰ متنِ بخشِ «جهان» (کارل سیگن، سنکا و…) با برچسبِ `Poet.SAADI` ذخیره
 * می‌شدند — یعنی نثرِ مدرنِ غربی به نامِ سعدی ثبت شده بود. این تست‌ها جلوی
 * بازگشتِ آن اشتباه را می‌گیرند.
 */
class PoetAttributionTest {

    @Test
    fun `world section is not attributed to a persian poet`() {
        assertEquals(Poet.WORLD, Collection.STORIES.poet)
        assertFalse("«جهان» نباید شاعرِ کلاسیک به حساب بیاید", Poet.WORLD.isClassical)
    }

    @Test
    fun `fal sources contain only classical poets`() {
        val sources = Poet.falSources
        assertTrue(sources.contains(Poet.HAFEZ))
        assertTrue(sources.contains(Poet.SAADI))
        assertTrue(sources.contains(Poet.RUMI))
        assertTrue(sources.contains(Poet.KHAYYAM))
        assertFalse("«جهان» نباید منبعِ فال باشد", sources.contains(Poet.WORLD))
        assertEquals(4, sources.size)
    }

    @Test
    fun `every classical collection belongs to a classical poet`() {
        Collection.entries
            .filter { it != Collection.STORIES }
            .forEach {
                assertTrue("${it.faName} باید به شاعرِ کلاسیک تعلق داشته باشد", it.poet.isClassical)
            }
    }

    @Test
    fun `poet keys round-trip`() {
        Poet.entries.forEach { assertEquals(it, Poet.fromKey(it.key)) }
        assertEquals(Poet.HAFEZ, Poet.fromKey("unknown"))
    }
}
