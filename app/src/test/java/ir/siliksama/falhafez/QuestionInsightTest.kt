package ir.siliksama.falhafez

import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.usecase.QuestionInsight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * خوانشِ نیّتِ کاربر.
 *
 * نکتهٔ مهم: هدف این نیست که تحلیل «درست» باشد به معنای زبان‌شناختی، بلکه این است
 * که دو پرسشِ متفاوت، دو تفسیرِ متفاوت بگیرند — چیزی که پیش‌تر نمی‌شد.
 */
class QuestionInsightTest {

    private val pickFirst: (List<String>) -> String = { it.first() }

    private fun analyze(q: String, c: FalCategory = FalCategory.NONE) =
        QuestionInsight.analyze(q, c)

    // ── شکلِ پرسش ───────────────────────────────────────────────────────────

    @Test
    fun `two-option question is a dilemma`() {
        assertEquals(QuestionInsight.Shape.DILEMMA, analyze("بمانم یا بروم؟").shape)
        assertEquals(QuestionInsight.Shape.DILEMMA, analyze("کدام پیشنهاد را قبول کنم").shape)
    }

    @Test
    fun `when-questions are timing questions`() {
        assertEquals(QuestionInsight.Shape.TIMING, analyze("چه زمانی نتیجه می‌گیرم؟").shape)
        assertEquals(QuestionInsight.Shape.TIMING, analyze("تا کی باید صبر کنم").shape)
    }

    @Test
    fun `what-should-i-do is guidance`() {
        assertEquals(QuestionInsight.Shape.GUIDANCE, analyze("چه کنم با این وضع").shape)
        assertEquals(QuestionInsight.Shape.GUIDANCE, analyze("چگونه شروع کنم").shape)
    }

    @Test
    fun `plain yes-no question is detected`() {
        assertEquals(QuestionInsight.Shape.YES_NO, analyze("آیا موفق می‌شوم؟").shape)
    }

    @Test
    fun `statement of intent stays open`() {
        assertEquals(QuestionInsight.Shape.OPEN, analyze("نیت کردم برای آرامش دلم").shape)
    }

    // ── لحن ─────────────────────────────────────────────────────────────────

    @Test
    fun `worry is detected`() {
        assertEquals(QuestionInsight.Tone.WORRIED, analyze("نگران نتیجه آزمایشم هستم").tone)
    }

    @Test
    fun `weariness is detected`() {
        assertEquals(QuestionInsight.Tone.WEARY, analyze("خسته شدم از این وضعیت").tone)
    }

    @Test
    fun `hope is detected`() {
        assertEquals(QuestionInsight.Tone.HOPEFUL, analyze("امیدوارم این بار جواب بدهد").tone)
    }

    // ── حدسِ دسته از متن ─────────────────────────────────────────────────────

    @Test
    fun `category is inferred when user picked none`() {
        assertEquals(FalCategory.TRAVEL, analyze("برای مهاجرت اقدام کنم؟").category)
        assertEquals(FalCategory.CAREER, analyze("این شغل جدید را قبول کنم").category)
        assertEquals(FalCategory.HEALTH, analyze("نتیجه درمان چه می‌شود").category)
        assertEquals(FalCategory.LOVE, analyze("آیا او دوستم دارد").category)
    }

    @Test
    fun `explicit category always wins over the guess`() {
        // کاربر «سلامتی» زده ولی از کار نوشته — انتخابِ صریحِ او مقدم است.
        val i = analyze("درباره شغلم پرسیدم", FalCategory.HEALTH)
        assertEquals(FalCategory.HEALTH, i.category)
    }

    // ── متنِ بی‌معنا ─────────────────────────────────────────────────────────

    @Test
    fun `empty or tiny input is not meaningful`() {
        assertFalse(analyze("").meaningful)
        assertFalse(analyze("   ").meaningful)
        assertFalse(analyze("سلام").meaningful)
        assertFalse(QuestionInsight.analyze(null, FalCategory.NONE).meaningful)
    }

    @Test
    fun `no text means no added sentences`() {
        val i = QuestionInsight.analyze(null, FalCategory.NONE)
        assertEquals("", QuestionInsight.acknowledgement(i, pickFirst))
        assertEquals("", QuestionInsight.guidance(i, pickFirst))
    }

    // ── خروجی واقعاً متفاوت است ──────────────────────────────────────────────

    @Test
    fun `different question shapes produce different guidance`() {
        val dilemma = QuestionInsight.guidance(analyze("بروم یا بمانم؟"), pickFirst)
        val timing = QuestionInsight.guidance(analyze("چه زمانی اتفاق می‌افتد؟"), pickFirst)
        val guide = QuestionInsight.guidance(analyze("چه کنم الان"), pickFirst)

        assertNotEquals(dilemma, timing)
        assertNotEquals(timing, guide)
        assertNotEquals(dilemma, guide)
        assertTrue(dilemma.isNotBlank() && timing.isNotBlank() && guide.isNotBlank())
    }

    @Test
    fun `tone changes the guidance even for the same shape`() {
        val calm = QuestionInsight.guidance(analyze("بروم یا بمانم؟"), pickFirst)
        val tired = QuestionInsight.guidance(analyze("خسته‌ام، بروم یا بمانم؟"), pickFirst)
        assertNotEquals(calm, tired)
        assertTrue(tired.length > calm.length)
    }

    @Test
    fun `arabic yeh and kaf are normalized`() {
        // متنی که با صفحه‌کلیدِ عربی تایپ شده باید مثل فارسی خوانده شود.
        assertEquals(FalCategory.CAREER, analyze("شغلي جديد را قبول كنم").category)
    }

    @Test
    fun `guidance never promises or threatens`() {
        val forbidden = listOf("قطعا", "حتما اتفاق", "خطر", "بدشانسی", "نفرین", "می‌میری")
        val samples = listOf(
            "بروم یا بمانم؟", "چه زمانی؟", "آیا موفق می‌شوم؟",
            "آیا او دوستم دارد؟", "چه کنم", "نیت کردم"
        )
        for (s in samples) {
            val text = QuestionInsight.acknowledgement(analyze(s), pickFirst) + " " +
                QuestionInsight.guidance(analyze(s), pickFirst)
            for (bad in forbidden) {
                assertFalse("«$bad» در متنِ «$s» آمد", text.contains(bad))
            }
        }
    }
}
