package ir.siliksama.falhafez.data.ads

import android.app.Activity

/**
 * لایهٔ انتزاعیِ تبلیغات — UI هرگز مستقیم با SDK کار نمی‌کند.
 * تعویض یا واسطه‌گریِ شبکه فقط در [ir.siliksama.falhafez.di.AdModule] عوض می‌شود.
 *
 * اپ آفلاین-محور است: بدون شبکه هیچ تبلیغی نمایش داده نمی‌شود و
 * **هیچ قابلیتی هم پشتِ تبلیغ قفل نمی‌ماند**.
 */
interface AdManager {

    /** وقتی true است که شبکهٔ تبلیغاتی پیکربندی شده و می‌تواند تبلیغ بدهد. */
    val enabled: Boolean

    suspend fun isNetworkAvailable(): Boolean

    /**
     * تبلیغ تمام‌صفحه (با سقفِ فرکانس). فقط **بعد از** خواندنِ کاملِ فال و هنگام بازگشت
     * به خانه صدا زده می‌شود — هرگز حینِ آیینِ فال. true یعنی واقعاً نمایش داده شد.
     */
    suspend fun showInterstitial(activity: Activity): Boolean

    /**
     * ویدیوی جایزه‌ای. [onReward] دقیقاً یک‌بار و فقط در صورتِ تماشای کامل صدا زده می‌شود.
     * true یعنی تبلیغ نمایش داده شد.
     */
    suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean

    /** به لایهٔ تبلیغات خبر می‌دهد که یک فال کامل شد (مبنای سقفِ فرکانس). */
    suspend fun onDrawCompleted()

    /**
     * گرم‌کردنِ لایهٔ تبلیغات (preload).
     * باید **بعد از** بارگذاریِ سطحِ حمایتِ کاربر صدا زده شود تا برای مشترکان
     * حتی یک درخواستِ بی‌مورد هم فرستاده نشود.
     */
    fun warmUp()

    /**
     * اگر گرم‌کردنِ اولیه به‌خاطرِ آفلاین‌بودن انجام نشده، دوباره تلاش کن.
     * هر بار که اپ به پیش‌زمینه برمی‌گردد صدا زده می‌شود.
     */
    fun retryWarmUpIfNeeded()
}
