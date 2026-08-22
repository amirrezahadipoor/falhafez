package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.model.DrawAccess
import ir.siliksama.falhafez.domain.model.SupportTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * قواعدِ اقتصادیِ فال و تبلیغات.
 *
 * این تست‌ها تضمین می‌کنند که قولِ اپ به کاربر شکسته نشود:
 *  • حمایت‌کننده هرگز تبلیغ نبیند.
 *  • آفلاین هرگز محدود نشود و تبلیغ نبیند.
 *  • ۲ فالِ رایگان در روز واقعاً ۲ تا باشد.
 */
class DrawAccessTest {

    @Test
    fun `supporter is unlimited and sees no ads`() {
        val a = DrawAccess.UNLIMITED_SUPPORTER
        assertTrue("حمایت‌کننده باید نامحدود باشد", a.isUnlimited)
        assertFalse("حمایت‌کننده نباید هیچ تبلیغی ببیند", a.adsApply)
    }

    @Test
    fun `offline is unlimited and shows no ads`() {
        val a = DrawAccess.UNLIMITED_OFFLINE
        assertTrue("آفلاین باید فالِ نامحدود بدهد", a.isUnlimited)
        assertFalse("آفلاین نباید تبلیغ داشته باشد", a.adsApply)
    }

    @Test
    fun `online free quota is limited but allows ads`() {
        val a = DrawAccess.FREE_QUOTA
        assertFalse(a.isUnlimited)
        assertTrue("در سهمیهٔ رایگانِ آنلاین، تبلیغِ بین‌صفحه‌ای مجاز است", a.adsApply)
    }

    @Test
    fun `exhausted quota needs unlock and ads apply`() {
        val a = DrawAccess.NEEDS_UNLOCK
        assertFalse(a.isUnlimited)
        assertTrue(a.adsApply)
    }

    @Test
    fun `daily free limit is two`() {
        // قولِ صفحهٔ فروشگاه و مستندات: دو فالِ رایگان در روز.
        assertEquals(2, ir.siliksama.falhafez.presentation.home.DAILY_FREE_LIMIT)
    }

    @Test
    fun `every paid tier removes ads`() {
        assertFalse("کاربر رایگان تبلیغ می‌بیند", SupportTier.NONE.adsRemoved)
        listOf(SupportTier.BASE, SupportTier.PLUS, SupportTier.GOLD).forEach {
            assertTrue("${it.faName} باید تبلیغات را حذف کند", it.adsRemoved)
        }
    }

    @Test
    fun `tier keys round-trip and unknown falls back to none`() {
        SupportTier.entries.forEach {
            assertEquals(it, SupportTier.fromKey(it.key))
        }
        assertEquals(SupportTier.NONE, SupportTier.fromKey("something-else"))
        assertEquals(SupportTier.NONE, SupportTier.fromKey(null))
    }
}
