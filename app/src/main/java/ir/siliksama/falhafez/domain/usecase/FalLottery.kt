package ir.siliksama.falhafez.domain.usecase

import kotlin.random.Random

/**
 * قرعهٔ فال — وزن‌دهیِ آگاهانه به‌جای انتخابِ کاملاً تصادفی.
 *
 * ## دو مشکلی که حل می‌کند
 *
 * ### ۱. حافظ در «فال حافظ» گم شده بود
 * استخرِ فال ۸٬۳۴۷ شعر دارد، اما توزیعش این است:
 *
 * | شاعر  | تعداد | سهم از انتخابِ تصادفی |
 * |-------|-------|------------------------|
 * | مولانا| 6,240 | **۷۴.۸٪** |
 * | سعدی  | 1,355 | ۱۶.۲٪ |
 * | حافظ  |   574 | **۶.۹٪** |
 * | خیام  |   178 | ۲.۱٪ |
 *
 * یعنی در حالتِ «همهٔ مجموعه‌ها»، از هر ۱۴ فال تنها یکی حافظ بود — در اپی که
 * نامش «فال حافظ» است و کاربر با انتظارِ حافظ می‌آید. علتش هم کیفیت نبود، صرفاً
 * این بود که دیوان شمس شعرِ بیشتری دارد.
 *
 * ### ۲. شعرهای بسیار بلند
 * ۱۵ شعر بیش از ۱۰۰ بیت دارند (بلندترین ۱۵۹ بیت). آمدنِ چنین متنی به‌عنوان «فال»
 * تجربه را خراب می‌کند: کاربر می‌خواهد پاسخِ نیّتش را بخواند، نه ۱۵۹ بیت را پیمایش
 * کند. از سوی دیگر تک‌بیتی‌ها (۲٬۴۴۵ مورد) هم برای فال کم‌مایه‌اند.
 *
 * ## راهکار
 * وزنِ نهاییِ هر شعر = وزنِ شاعر × وزنِ اندازه. انتخاب با چرخِ رولت.
 * هیچ شعری حذف نمی‌شود — فقط شانسِ نسبی تنظیم می‌شود؛ پس کلِ دیوان همچنان
 * در دسترس است و «کمبودِ محتوا» ایجاد نمی‌شود.
 */
object FalLottery {

    /**
     * سهمِ هدف برای هر شاعر در حالتِ «همهٔ مجموعه‌ها».
     *
     * حافظ بیشترین سهم را دارد چون هویتِ اپ است و کاربر با نیّتِ حافظ می‌آید؛
     * بقیه سهمِ معناداری دارند تا تنوع حفظ شود و گنجینهٔ ۸ هزار شعری هدر نرود.
     */
    private val POET_SHARE = mapOf(
        "hafez" to 0.40,
        "rumi" to 0.25,
        "saadi" to 0.22,
        "khayyam" to 0.13
    )

    /** محدودهٔ آرمانیِ طولِ یک فال (بیت). */
    private const val IDEAL_MIN = 5
    private const val IDEAL_MAX = 20

    /**
     * وزنِ اندازه: شعرهای خیلی کوتاه و خیلی بلند شانسِ کمتری می‌گیرند، ولی
     * هرگز صفر نمی‌شوند (کفِ ۰.۱۵) تا هیچ بخشی از دیوان از دسترس خارج نشود.
     */
    fun sizeWeight(beits: Int): Double = when {
        beits <= 0 -> 0.15
        beits < IDEAL_MIN -> 0.35 + 0.13 * beits      // ۱ بیت → ۰.۴۸ ، ۴ بیت → ۰.۸۷
        beits <= IDEAL_MAX -> 1.0                      // محدودهٔ آرمانی
        beits <= 40 -> 0.65
        beits <= 70 -> 0.35
        else -> 0.15                                   // مثنویِ بلند: کمیاب، نه ناممکن
    }

    /**
     * وزنِ هر شعر از یک شاعر.
     *
     * سهمِ هدفِ شاعر بر تعدادِ شعرهایش تقسیم می‌شود؛ در نتیجه مجموعِ وزنِ شعرهای
     * هر شاعر برابرِ سهمِ هدفش می‌شود — مستقل از اینکه چند شعر دارد.
     */
    fun poetWeight(poet: String, poemsOfThatPoet: Int): Double {
        if (poemsOfThatPoet <= 0) return 0.0
        val share = POET_SHARE[poet] ?: 0.05
        return share / poemsOfThatPoet
    }

    /**
     * انتخابِ وزن‌دار.
     *
     * @param items نامزدها: (شناسه، کلیدِ شاعر، تعدادِ بیت)
     * @param weightByPoet وقتی true است سهمِ شاعران متعادل می‌شود (حالتِ «همه»).
     *        در حالتِ تک‌شاعر بی‌معناست و فقط وزنِ اندازه اعمال می‌شود.
     */
    fun pick(
        items: List<Triple<Long, String, Int>>,
        weightByPoet: Boolean,
        random: Random = Random.Default
    ): Long? {
        if (items.isEmpty()) return null
        if (items.size == 1) return items[0].first

        val perPoetCount: Map<String, Int> =
            if (weightByPoet) items.groupingBy { it.second }.eachCount() else emptyMap()

        var total = 0.0
        val weights = DoubleArray(items.size)
        for (i in items.indices) {
            val (_, poet, beits) = items[i]
            val base = if (weightByPoet) poetWeight(poet, perPoetCount[poet] ?: 1) else 1.0
            val w = base * sizeWeight(beits)
            weights[i] = w
            total += w
        }
        if (total <= 0.0) return items.random(random).first

        // چرخِ رولت
        var r = random.nextDouble() * total
        for (i in items.indices) {
            r -= weights[i]
            if (r <= 0.0) return items[i].first
        }
        return items.last().first
    }

    /**
     * تناسبِ موضوعیِ هر تگ با دستهٔ نیّتِ کاربر.
     *
     * وزنِ ۱.۰ = بی‌ربط (ولی ممکن)، عددهای بزرگ‌تر = شانسِ بیشتر.
     * عمداً «فیلتر» نیست بلکه «تمایل» است: اگر فقط شعرهای هم‌موضوع را نشان
     * دهیم، استخر کوچک می‌شود، تکرار زیاد می‌شود و مهم‌تر اینکه فال از حالتِ
     * قرعه درمی‌آید. با وزن‌دهی، شعرِ مرتبط محتمل‌تر است ولی قرعه قرعه می‌ماند.
     */
    private val CATEGORY_AFFINITY: Map<String, Map<String, Double>> = mapOf(
        "love" to mapOf(
            "love" to 6.0, "hope" to 2.0, "joy" to 1.6,
            "patience" to 1.4, "compassion" to 1.5
        ),
        "career" to mapOf(
            "effort" to 6.0, "wisdom" to 3.0, "patience" to 2.2,
            "hope" to 1.8, "new-beginnings" to 1.6, "decision" to 1.5
        ),
        "travel" to mapOf(
            "travel" to 7.0, "new-beginnings" to 2.5, "hope" to 1.8,
            "effort" to 1.5, "detachment" to 1.4
        ),
        "health" to mapOf(
            "patience" to 5.0, "hope" to 4.0, "faith" to 3.0,
            "compassion" to 2.0, "detachment" to 1.6
        ),
        "decision" to mapOf(
            "decision" to 7.0, "wisdom" to 5.0, "faith" to 2.0,
            "patience" to 1.6, "new-beginnings" to 1.5
        )
    )

    /**
     * تگ‌هایی که به‌طور طبیعی پرجمعیت‌اند و اگر مهار نشوند، در هر دسته‌ای غالب
     * می‌شوند. «عشق» ۲٬۷۰۴ شعر و «شادی» ۲٬۵۰۱ شعر از ۸٬۳۴۷ شعرِ استخر دارند؛
     * بدونِ این ضریب، حتی در «فالِ تصمیم» هم بیشترین سهم مالِ عشق می‌شد.
     */
    private val THEME_BALANCE = mapOf(
        "love" to 0.30,
        "joy" to 0.35
    )

    /** وزنِ موضوعی برای دستهٔ [category] و تگِ [theme]. */
    fun categoryWeight(category: String, theme: String): Double {
        val balance = THEME_BALANCE[theme] ?: 1.0
        if (category.isBlank() || category == "none") return balance
        val affinity = CATEGORY_AFFINITY[category]?.get(theme)
        return if (affinity != null) {
            // وقتی تگ واقعاً به دستهٔ کاربر مربوط است، مهارِ جمعیتی اعمال نمی‌شود
            // (در «فالِ عشق»، شعرِ عاشقانه باید غالب باشد).
            affinity
        } else {
            balance
        }
    }

    /**
     * قرعهٔ کامل: وزنِ شاعر × وزنِ اندازه × تناسبِ موضوعی با نیّتِ کاربر.
     *
     * @param items (شناسه، شاعر، تگِ موضوعی، تعدادِ بیت)
     * @param category کلیدِ دستهٔ فال («love»، «career»، … یا «none»)
     */
    fun pickThemed(
        items: List<Quad>,
        weightByPoet: Boolean,
        category: String,
        random: Random = Random.Default
    ): Long? {
        if (items.isEmpty()) return null
        if (items.size == 1) return items[0].id

        val perPoetCount: Map<String, Int> =
            if (weightByPoet) items.groupingBy { it.poet }.eachCount() else emptyMap()

        var total = 0.0
        val weights = DoubleArray(items.size)
        for (i in items.indices) {
            val it0 = items[i]
            val base = if (weightByPoet) poetWeight(it0.poet, perPoetCount[it0.poet] ?: 1) else 1.0
            val w = base * sizeWeight(it0.beits) * categoryWeight(category, it0.theme)
            weights[i] = w
            total += w
        }
        if (total <= 0.0) return items.random(random).id

        var r = random.nextDouble() * total
        for (i in items.indices) {
            r -= weights[i]
            if (r <= 0.0) return items[i].id
        }
        return items.last().id
    }

    /** ردیفِ نامزد با تگِ موضوعی. */
    data class Quad(
        val id: Long,
        val poet: String,
        val theme: String,
        val beits: Int
    )
}
