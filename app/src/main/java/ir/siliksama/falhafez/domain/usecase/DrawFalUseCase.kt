package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.domain.model.DrawEntry
import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.DrawRepository
import ir.siliksama.falhafez.domain.repository.PoemRepository
import javax.inject.Inject

/**
 * گرفتنِ فال: شعری با قرعهٔ **وزن‌دار** انتخاب و در تاریخچه ثبت می‌شود.
 *
 * وزن‌دهی سه عامل دارد ([FalLottery]):
 *  ۱. سهمِ شاعر — تا حافظ در حالتِ «همه» گم نشود (پیش‌تر ۶.۹٪ بود).
 *  ۲. اندازهٔ شعر — متنِ ۱۵۹ بیتی فالِ خوبی نیست.
 *  ۳. **تناسب با دستهٔ نیّتِ کاربر** — پیش‌تر دسته فقط ذخیره می‌شد و هیچ اثری
 *     بر انتخاب نداشت؛ یعنی «فالِ عشق» و «فالِ سفر» یک قرعهٔ یکسان بودند.
 *
 * اگر کاربر دسته انتخاب نکرده باشد، [QuestionInsight] آن را از متنِ پرسش حدس می‌زند.
 *
 * ۳۰ فالِ اخیر کنار گذاشته می‌شوند تا تکرارِ فوری پیش نیاید.
 * [source] = null یعنی از همهٔ دیوان‌ها؛ وگرنه فقط دیوانِ همان شاعر.
 */
class DrawFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository,
    private val drawRepository: DrawRepository
) {
    suspend operator fun invoke(
        question: String?,
        category: FalCategory,
        source: Poet? = Poet.HAFEZ
    ): DrawEntry? {
        val recentIds = drawRepository.recentPoemIds(limit = 30)

        // اگر کاربر دسته‌ای انتخاب نکرده اما نیّتش را نوشته، دسته از متنِ پرسش
        // استنباط می‌شود؛ «می‌خواهم مهاجرت کنم» بدونِ هیچ انتخابی، فالِ سفر است.
        val effectiveCategory = QuestionInsight.analyze(question, category).category

        val poem = poemRepository.getRandomPoemFor(
            category = effectiveCategory,
            excludeIds = recentIds,
            poet = source
        ) ?: return null
        val drawId = drawRepository.record(poem.id, question, category)
        return DrawEntry(
            id = drawId,
            poem = poem,
            question = question,
            category = category,
            drawnAt = System.currentTimeMillis()
        )
    }
}
