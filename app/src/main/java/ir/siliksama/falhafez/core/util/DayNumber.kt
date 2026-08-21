package ir.siliksama.falhafez.core.util

import java.util.Calendar

/**
 * شمارهٔ روزِ «محلی» (بر پایهٔ نیمه‌شبِ محلیِ دستگاه).
 *
 * فالِ روز، ویجتِ «بیتِ امروز» و زنجیرهٔ روزانهٔ کارنامه باید همه از یک
 * مرزِ روز استفاده کنند وگرنه هرکدام نیمه‌شبِ متفاوتی را مبنا می‌گیرند
 * (UTC در برابر محلی) و «فالِ امروز» در اپ و ویجت یکی نمی‌شود.
 */
object DayNumber {

    /** شمارهٔ روزِ محلی برای لحظهٔ داده‌شده (پیش‌فرض: اکنون). */
    fun local(epochMillis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis / 86_400_000L
    }
}
