package ir.siliksama.falhafez.core.util

/**
 * منطقِ خالصِ کارنامهٔ فال — بدون وابستگی به اندروید تا قابلِ آزمونِ واحد باشد.
 * «روز» در اینجا شمارهٔ روزِ محلی است (ببینید [DayNumber]).
 */
object FalStats {

    /**
     * زنجیرهٔ فعلی: تعداد روزهای پشت‌سرهم که فال گرفته شده و به امروز (یا دیروز)
     * ختم می‌شوند. ورودی می‌تواند نامرتب یا تکراری باشد — داخل تابع یکتا و نزولی می‌شود.
     */
    fun currentStreak(dayNumbers: List<Long>, today: Long): Int {
        val days = dayNumbers.distinct().sortedDescending()
        if (days.isEmpty()) return 0
        val newest = days.first()
        if (newest != today && newest != today - 1) return 0
        var streak = 0
        var expected = newest
        for (d in days) {
            if (d == expected) {
                streak++
                expected -= 1
            } else if (d < expected) {
                break
            }
        }
        return streak
    }

    /** بهترین زنجیرهٔ ثبت‌شده در کلِ تاریخچه (روزهای متوالی). */
    fun bestStreak(dayNumbers: List<Long>): Int {
        var best = 0
        var run = 0
        var prev: Long? = null
        for (d in dayNumbers.sorted()) {
            run = if (prev != null && d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }
        return best
    }
}
