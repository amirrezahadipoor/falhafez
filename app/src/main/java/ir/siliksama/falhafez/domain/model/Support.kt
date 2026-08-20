package ir.siliksama.falhafez.domain.model

/**
 * یک‌بار حمایت مالی = حذف دائمی تبلیغات.
 * هر سه سطح، تبلیغات را برای همیشه حذف می‌کنند؛ سطح‌های بالاتر امکانات بیشتری می‌دهند.
 */
enum class SupportTier(
    val key: String,
    val faName: String,
    val priceToman: Int,
    val perks: String
) {
    NONE("none", "—", 0, ""),

    BASE("base", "حمایتِ پایه", 100_000,
        "حذفِ کاملِ تبلیغات، برای همیشه"),

    PLUS("plus", "حمایتِ ویژه", 300_000,
        "حذف تبلیغات + قالبِ ویژهٔ «شبِ یلدا» + ۵ فالِ اضافه در روز"),

    GOLD("gold", "حمایتِ همیشگی", 490_000,
        "حذف تبلیغات + قالب یلدا + فالِ بدونِ درنگ (بدون ضرب‌آهنگ) + ۲۰ فالِ اضافه در روز + نشانِ «پشتیبان» روی تصویرِ اشتراک");

    val adsRemoved: Boolean get() = this != NONE

    companion object {
        fun fromKey(key: String?): SupportTier = entries.firstOrNull { it.key == key } ?: NONE
    }
}
