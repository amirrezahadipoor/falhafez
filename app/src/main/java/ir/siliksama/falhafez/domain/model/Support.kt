package ir.siliksama.falhafez.domain.model

/**
 * یک‌بار حمایت مالی = حذف دائمی تبلیغات.
 * هر سه سطح، تبلیغات را برای همیشه حذف می‌کنند؛ سطح‌های بالاتر امکانات بیشتری می‌دهند.
 */
enum class SupportTier(
    val key: String,
    val faName: String,
    val priceToman: Int,
    val sku: String,
    val perks: String
) {
    NONE("none", "—", 0, "", ""),

    BASE("base", "حمایتِ پایه", 100_000, "fal_support_base",
        "حذف تبلیغات + فالِ نامحدود + حمایت مالی از سازنده"),

    PLUS("plus", "حمایتِ ویژه", 290_000, "fal_support_plus",
        "حذف تبلیغات + فالِ نامحدود + قرار گرفتن نام و کانالِ شما روی فالِ اشتراکی + حمایت مالی از سازنده"),

    GOLD("gold", "حمایتِ همیشگی", 490_000, "fal_support_gold",
        "حذف تبلیغات + فالِ نامحدود + نام و کانالِ شما روی فالِ اشتراکی + حمایت مالی از سازنده + افزودنِ قابلیتِ دلخواهِ شما به اپلیکیشن");

    val adsRemoved: Boolean get() = this != NONE

    /** بدونِ ضرب‌آهنگِ انتظار (فالِ فوری) — قابلیتِ ویژهٔ «حمایتِ همیشگی». */
    val instantDraw: Boolean get() = this == GOLD

    /** نام و کانالِ کاربر روی فالِ اشتراکی نقش می‌بندد. */
    val showsChannel: Boolean get() = this == PLUS || this == GOLD

    companion object {
        fun fromKey(key: String?): SupportTier = entries.firstOrNull { it.key == key } ?: NONE
    }
}
