package ir.siliksama.falhafez.core.util

/**
 * پاک‌سازیِ ورودیِ جستجو برای FTS4 (SQLite MATCH).
 *
 * FTS4 عبارت‌هایی مثل «(شعر*»، «-»، «:»، «^» یا واژهٔ «AND» را به‌عنوان عملگر
 * می‌خواند و در برخورد با آن‌ها «malformed MATCH expression» پرتاب می‌کند.
 * این تابع هر آنچه را که عملگر است حذف می‌کند و فقط توکن‌های فارسی/رقم را با
 * پیشوندِ «*» برمی‌گرداند؛ اگر هیچ توکنِ قابل جستجویی نماند null برمی‌گرداند.
 *
 * کاملاً خالص (بدون وابستگی به اندروید) تا در آزمونِ واحد قفل شود.
 */
object SearchSanitizer {

    private val PERSIAN_OR_DIGIT = { c: Char -> c.code in 0x0600..0x06FF || c.isDigit() }

    fun sanitize(raw: String): String? {
        val safe = raw
            .map { c ->
                if (c.isLetterOrDigit() || c.isWhitespace() || c == '\u200C' || c == '\u200D') c else ' '
            }
            .joinToString("")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (safe.isBlank()) return null

        val tokens = safe.split(" ")
            .filter { t -> t.any(PERSIAN_OR_DIGIT) }
        if (tokens.isEmpty()) return null

        return tokens.joinToString(" ") { "$it*" }
    }
}
