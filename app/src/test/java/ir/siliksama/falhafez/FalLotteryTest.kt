package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.usecase.FalLottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * قرعهٔ فال باید «هوشمند» باشد: حافظ در اپِ حافظ گم نشود، و متنِ ۱۵۹ بیتی
 * به‌عنوان فال جلوی کاربر باز نشود — بدونِ آنکه هیچ شعری از دسترس خارج شود.
 */
class FalLotteryTest {

    /** استخری با همان نسبت‌های واقعیِ دیوان‌ها. */
    private fun realisticPool(): List<Triple<Long, String, Int>> {
        var id = 1L
        val out = mutableListOf<Triple<Long, String, Int>>()
        fun add(poet: String, n: Int, beits: Int) {
            repeat(n) { out += Triple(id++, poet, beits) }
        }
        add("hafez", 574, 8)
        add("saadi", 1_355, 9)
        add("rumi", 6_240, 10)
        add("khayyam", 178, 2)
        return out
    }

    @Test
    fun `hafez gets a meaningful share in all-collections mode`() {
        val pool = realisticPool()
        val rnd = Random(42)
        val counts = mutableMapOf<String, Int>()
        repeat(20_000) {
            val id = FalLottery.pick(pool, weightByPoet = true, random = rnd)!!
            val poet = pool.first { it.first == id }.second
            counts[poet] = (counts[poet] ?: 0) + 1
        }
        val hafez = counts["hafez"]!! / 20_000.0
        // بدونِ وزن‌دهی این عدد ۶.۹٪ بود.
        assertTrue("سهمِ حافظ خیلی کم است: ${"%.1f".format(hafez * 100)}%", hafez > 0.30)
        assertTrue("سهمِ حافظ خیلی زیاد است: ${"%.1f".format(hafez * 100)}%", hafez < 0.60)

        // هیچ شاعری نباید حذف شود.
        listOf("rumi", "saadi", "khayyam").forEach {
            assertTrue("$it هیچ سهمی نگرفت", (counts[it] ?: 0) > 0)
        }
    }

    @Test
    fun `single poet mode ignores poet weighting`() {
        val pool = (1L..50L).map { Triple(it, "hafez", 8) }
        val picked = FalLottery.pick(pool, weightByPoet = false, random = Random(7))
        assertTrue(picked in pool.map { it.first })
    }

    @Test
    fun `very long poems are rare but never impossible`() {
        assertTrue(FalLottery.sizeWeight(159) > 0.0)
        assertTrue(FalLottery.sizeWeight(120) < FalLottery.sizeWeight(12))
        assertTrue(FalLottery.sizeWeight(80) < FalLottery.sizeWeight(30))
    }

    @Test
    fun `ideal length range has full weight`() {
        (5..20).forEach { assertEquals(1.0, FalLottery.sizeWeight(it), 1e-9) }
    }

    @Test
    fun `very short poems are down-weighted but reachable`() {
        val w = FalLottery.sizeWeight(1)
        assertTrue(w > 0.0)
        assertTrue(w < 1.0)
    }

    @Test
    fun `empty pool returns null and single item returns itself`() {
        assertNull(FalLottery.pick(emptyList(), weightByPoet = true))
        assertEquals(9L, FalLottery.pick(listOf(Triple(9L, "hafez", 8)), weightByPoet = true))
    }

    @Test
    fun `every poem in the pool stays reachable`() {
        // هیچ شعری نباید وزنِ صفر بگیرد — «کمبود محتوا» ممنوع.
        listOf(0, 1, 3, 8, 25, 50, 100, 159).forEach {
            assertTrue("بیت=$it وزنِ صفر گرفت", FalLottery.sizeWeight(it) > 0.0)
        }
    }

    // ── تطبیقِ فال با دستهٔ نیّتِ کاربر ──────────────────────────────────────

    private fun themedPool(): List<FalLottery.Quad> {
        var id = 1L
        val out = mutableListOf<FalLottery.Quad>()
        fun add(theme: String, n: Int) {
            repeat(n) { out += FalLottery.Quad(id++, "hafez", theme, 8) }
        }
        // نسبت‌های واقعیِ استخر
        add("love", 2_704); add("joy", 2_501); add("patience", 685)
        add("effort", 652); add("wisdom", 493); add("faith", 392)
        add("hope", 308); add("detachment", 191); add("travel", 179)
        add("new-beginnings", 109); add("decision", 78); add("compassion", 55)
        return out
    }

    private fun themeShare(category: String, theme: String, draws: Int = 12_000): Double {
        val pool = themedPool()
        val byId = pool.associateBy { it.id }
        val rnd = Random(11)
        var hits = 0
        repeat(draws) {
            val id = FalLottery.pickThemed(pool, weightByPoet = false, category = category, random = rnd)!!
            if (byId[id]!!.theme == theme) hits++
        }
        return hits.toDouble() / draws
    }

    @Test
    fun `travel category actually surfaces travel poems`() {
        // «سفر» فقط ۲.۱٪ استخر است؛ بدونِ وزن‌دهی عملاً هرگز نمی‌آمد.
        val share = themeShare("travel", "travel")
        assertTrue("سهمِ سفر در فالِ سفر فقط ${"%.1f".format(share * 100)}%", share > 0.12)
    }

    @Test
    fun `decision category surfaces wisdom and decision poems`() {
        val wisdom = themeShare("decision", "wisdom")
        assertTrue("خرد در فالِ تصمیم کم است: ${"%.1f".format(wisdom * 100)}%", wisdom > 0.20)
    }

    @Test
    fun `love category is dominated by love poems`() {
        assertTrue(themeShare("love", "love") > 0.55)
    }

    @Test
    fun `crowded themes do not dominate unrelated categories`() {
        // «عشق» ۳۲٪ استخر است. در فالِ تصمیم نباید موضوعِ غالب باشد.
        val love = themeShare("decision", "love")
        val wisdom = themeShare("decision", "wisdom")
        assertTrue("عشق در فالِ تصمیم غالب شد ($love vs $wisdom)", love < wisdom)
    }

    @Test
    fun `no category still returns a balanced spread`() {
        val love = themeShare("none", "love")
        assertTrue("بدونِ دسته، عشق بیش از حد غالب است: ${"%.1f".format(love * 100)}%", love < 0.32)
    }

    @Test
    fun `unknown category behaves like none`() {
        assertEquals(
            FalLottery.categoryWeight("none", "wisdom"),
            FalLottery.categoryWeight("something-odd", "wisdom"),
            1e-9
        )
    }
}
