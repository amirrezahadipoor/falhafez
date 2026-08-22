package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.core.util.DayNumber
import ir.siliksama.falhafez.domain.model.Collection
import ir.siliksama.falhafez.domain.model.Poem
import ir.siliksama.falhafez.domain.model.Poet
import ir.siliksama.falhafez.domain.repository.PoemRepository
import javax.inject.Inject

/**
 * فالِ روز — یک غزلِ حافظ که **همهٔ کاربران در یک روز یکسان می‌بینند**.
 * آیینِ مشترکی که گفتگو می‌سازد («فال امروزت چی اومد؟»). کاملاً آفلاین و تکرارپذیر.
 *
 * ## چرا نه `day % count`؟
 * پیاده‌سازیِ قبلی اندیس را از `dayNumber % count` می‌گرفت. این یعنی **فالِ فردا
 * همیشه غزلِ بعدیِ فالِ امروز بود** — کاربر بعد از چند روز الگو را می‌فهمید و کلِ
 * حسِ «قرعه» از بین می‌رفت. ضمناً چون شمارش با `ORDER BY id` و `OFFSET` انجام
 * می‌شد، ترتیب هم دقیقاً ترتیبِ دیوان بود.
 *
 * حالا شمارهٔ روز از یک **هشِ آمیزنده (splitmix64)** می‌گذرد: خروجی برای هر روز
 * قطعی و یکسان است (همه یک فال می‌بینند و ویجت با اپ هماهنگ می‌ماند)، اما
 * پراکندگی‌اش تصادفی به نظر می‌رسد و از روی فالِ امروز نمی‌شود فالِ فردا را حدس زد.
 */
class DailyFalUseCase @Inject constructor(
    private val poemRepository: PoemRepository
) {
    suspend fun today(): Poem? = poemForDay(DayNumber.local())

    /** همان انتخابِ قطعی — ویجتِ صفحهٔ خانه هم از این استفاده می‌کند. */
    suspend fun poemForDay(dayNumber: Long): Poem? {
        val count = poemRepository.countForPoetCollection(Poet.HAFEZ, Collection.HAFEZ_GHAZAL)
        if (count <= 0) return null
        return poemRepository.getPoemAtForCollection(
            Poet.HAFEZ,
            Collection.HAFEZ_GHAZAL,
            indexForDay(dayNumber, count)
        )
    }

    companion object {
        /** شمارهٔ روزِ محلی — مبنای مشترکِ فالِ روز در اپ و ویجت. */
        fun todayDayNumber(): Long = DayNumber.local()

        /**
         * اندیسِ غزلِ روز.
         *
         * قطعی است (ورودیِ یکسان ⇒ خروجیِ یکسان) ولی ترتیبی نیست.
         * از splitmix64 استفاده می‌کند: ارزان، بدونِ وابستگی، و پراکندگیِ خوب —
         * حتی برای روزهای پشتِ‌سرِ هم که ورودی‌شان فقط یک واحد فرق دارد.
         */
        fun indexForDay(dayNumber: Long, count: Int): Int {
            if (count <= 0) return 0
            var z = dayNumber * -0x61c8864680b583ebL + -0x7ee3623a03d3c83fL
            z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
            z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
            z = z xor (z ushr 31)
            // بیتِ علامت را می‌اندازیم تا باقی‌مانده هرگز منفی نشود
            return ((z ushr 1) % count.toLong()).toInt()
        }
    }
}
