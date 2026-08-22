package ir.siliksama.falhafez.domain.usecase

import ir.siliksama.falhafez.domain.model.FalCategory
import ir.siliksama.falhafez.domain.model.Poem
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * شخصی‌سازیِ تفسیرِ فال — **بدونِ دست‌بردن در معنای اصیلِ شعر**.
 *
 * ## فلسفهٔ کار
 * تفسیرِ درونِ دیوان، معنای واقعی و ثابتِ شعر است و حق نداریم عوضش کنیم.
 * آنچه شخصی می‌شود، **قابِ اطرافِ آن** است:
 *
 *   ‏[سرآغازِ متناسب با نیّت]  →  [تفسیرِ اصیلِ شعر — دست‌نخورده]  →  [پیوندِ عملی با پرسشِ کاربر]
 *
 * یعنی همان شعر و همان معنا، اما طوری خوانده می‌شود که انگار به پرسشِ همین کاربر
 * پاسخ می‌دهد. این تفاوتِ «فالِ هوشمند» با «متنِ ثابت» است.
 *
 * ## چرا قطعی و تکرارپذیر؟
 * خروجی با «بذر»ی از شناسهٔ فال ساخته می‌شود، پس اگر کاربر همان فال را در تاریخچه
 * دوباره باز کند، **دقیقاً همان متن** را می‌بیند. فالی که هر بار حرفِ دیگری بزند،
 * اعتمادِ کاربر را از بین می‌برد.
 *
 * ## آنچه هرگز انجام نمی‌دهد
 * وعدهٔ قطعی نمی‌دهد، نمی‌ترساند، و ادعای پیش‌گوییِ رویدادِ مشخص نمی‌کند —
 * فال آینه است، نه حکم.
 */
@Singleton
class PersonalizeTafsir @Inject constructor() {

    /**
     * @param poem شعرِ درآمده (تفسیرِ اصیلش دست‌نخورده می‌ماند)
     * @param question نیّتِ کاربر (ممکن است خالی باشد)
     * @param category دستهٔ فال
     * @param seed شناسهٔ یکتای این فال — تضمینِ ثباتِ متن در بازدیدهای بعدی
     */
    operator fun invoke(
        poem: Poem,
        question: String?,
        category: FalCategory,
        seed: Long
    ): String {
        val q = question?.trim().orEmpty()
        val rnd = java.util.Random(seed * 31 + poem.id)

        val opening = openingFor(category, q, poem, rnd)
        val closing = closingFor(category, q, poem, rnd)

        return buildString {
            if (opening.isNotBlank()) {
                append(opening)
                append("\n\n")
            }
            append(poem.tafsir.trim())   // ← معنای اصیل، بی‌کم‌وکاست
            if (closing.isNotBlank()) {
                append("\n\n")
                append(closing)
            }
        }
    }

    // ── سرآغاز: نیّت را به رسمیت می‌شناسد ───────────────────────────────────

    private fun openingFor(
        category: FalCategory,
        question: String,
        poem: Poem,
        rnd: java.util.Random
    ): String {
        val timeWord = timeOfDayWord()

        // اگر کاربر نیّتش را نوشته، مستقیم به آن اشاره می‌کنیم — این قوی‌ترین
        // نشانهٔ «فال برای من است» برای کاربر است.
        if (question.length >= 4) {
            val shortQ = question.take(60).trimEnd('؟', '?', '.', '،')
            val variants = listOf(
                "برای آنچه پرسیدی — «$shortQ» — دیوان این را گشود:",
                "نیّتت را نگه داشتی: «$shortQ». پاسخ از ${poem.poet.faName} چنین آمد:",
                "پرسشت این بود: «$shortQ». این شعر جوابِ امروزِ توست:",
                "با نیّتِ «$shortQ» فال گرفتی؛ آنچه آمد این است:"
            )
            return variants[abs(rnd.nextInt()) % variants.size]
        }

        // بدونِ نیّتِ نوشته‌شده → از دسته و زمانِ روز کمک می‌گیریم
        val byCategory = when (category) {
            FalCategory.LOVE -> listOf(
                "دلت درگیرِ کسی است و همین تو را به دیوان کشانده.",
                "فالِ عشق گرفتی؛ ${poem.poet.faName} در این باب کم نگفته است."
            )
            FalCategory.CAREER -> listOf(
                "پرسشت از کار و راهِ روزی است.",
                "فالِ کار گرفتی — و کار، صبر و تدبیر می‌خواهد."
            )
            FalCategory.TRAVEL -> listOf(
                "قصدِ رفتن داری یا در راهی.",
                "فالِ سفر گرفتی؛ راه، همیشه بیش از مقصد می‌آموزد."
            )
            FalCategory.HEALTH -> listOf(
                "نگرانِ تن یا جانِ کسی هستی.",
                "فالِ سلامتی گرفتی — و آرامشِ دل، نیمی از درمان است."
            )
            FalCategory.DECISION -> listOf(
                "بر سرِ دوراهی ایستاده‌ای.",
                "فالِ تصمیم گرفتی؛ انتخاب، سخت‌ترین کارِ آدمی است."
            )
            FalCategory.NONE -> listOf(
                "$timeWord نیّت کردی و دیوان را گشودی.",
                "بی‌آنکه چیزی بپرسی، دلت را به دستِ ${poem.poet.faName} سپردی."
            )
        }
        return byCategory[abs(rnd.nextInt()) % byCategory.size]
    }

    // ── پیوندِ پایانی: تفسیر را به کارِ امروزِ کاربر گره می‌زند ───────────────

    private fun closingFor(
        category: FalCategory,
        question: String,
        poem: Poem,
        rnd: java.util.Random
    ): String {
        val hasQuestion = question.length >= 4

        val bank = when (category) {
            FalCategory.LOVE -> listOf(
                "در کارِ دل، آنچه نگفته می‌ماند سنگین‌تر از آن است که گفته می‌شود.",
                "پیش از آنکه از او انتظار داشته باشی، ببین خودت چه داده‌ای.",
                "این فال نه «بله» می‌گوید نه «نه»؛ می‌گوید صادق باش و ببین چه می‌شود."
            )
            FalCategory.CAREER -> listOf(
                "در کارت، یک قدمِ کوچکِ امروز از یک نقشهٔ بزرگِ فردا کارسازتر است.",
                "آنچه دنبالش هستی نزدیک‌تر از آن است که فکر می‌کنی، اما رایگان نیست.",
                "به‌جای انتظار برای فرصتِ کامل، از همین که داری شروع کن."
            )
            FalCategory.TRAVEL -> listOf(
                "اگر قصدِ رفتن داری، بارت را سبک کن — هم بارِ دست، هم بارِ دل.",
                "این جابه‌جایی به سودِ توست، حتی اگر اولش سخت بنماید.",
                "همراهت را درست انتخاب کن؛ راه با آدمِ نادرست، دوبرابر می‌شود."
            )
            FalCategory.HEALTH -> listOf(
                "به تنت گوش بده؛ آنچه می‌گوید را نادیده نگیر.",
                "آرامشِ دل نیمی از سلامتی است — همان نیمی که در اختیارِ توست.",
                "این نگرانی طبیعی است، اما نگذار جای درمان را بگیرد."
            )
            FalCategory.DECISION -> listOf(
                "میانِ دو راه، آن را برگزین که فردا از گفتنش شرمنده نشوی.",
                "پاسخ را می‌دانی؛ فقط دنبالِ کسی می‌گردی که تأییدش کند.",
                "تصمیم نگرفتن هم تصمیم است — و معمولاً پرهزینه‌ترینش."
            )
            FalCategory.NONE -> listOf(
                "این فال چیزی را حتمی نمی‌کند؛ فقط آینه‌ای پیشِ روی تو می‌گذارد.",
                "آنچه خواندی را به حالِ خودت گره بزن — معنا آنجا کامل می‌شود.",
                "${poem.poet.faName} راه را نشان می‌دهد؛ رفتن با توست."
            )
        }

        val line = bank[abs(rnd.nextInt()) % bank.size]

        // اگر نیّت نوشته شده، یک جملهٔ بازتابی هم اضافه می‌کنیم
        return if (hasQuestion) {
            val reflect = listOf(
                "پیش از آنکه فال را ببندی، یک بار دیگر پرسشت را بخوان؛ گاهی خودِ پرسش نیمی از پاسخ است.",
                "این را با پرسشِ خودت بسنج — اگر جور درنیامد، شاید پرسش باید عوض شود، نه پاسخ.",
                "همین امروز یک کارِ کوچک در جهتِ آنچه خواستی انجام بده؛ فال با عمل کامل می‌شود."
            )
            line + "\n" + reflect[abs(rnd.nextInt()) % reflect.size]
        } else {
            line
        }
    }

    private fun timeOfDayWord(): String =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "این بامداد"
            in 11..15 -> "این نیم‌روز"
            in 16..19 -> "این عصر"
            in 20..23 -> "این شب"
            else -> "در این دلِ شب"
        }
}
