package ir.siliksama.falhafez.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import ir.siliksama.falhafez.core.util.SupportStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * شبکهٔ تبلیغاتی تپسل (اصلی برای بازار ایران). تا وقتی کلید تپسل در [AdConfig]
 * قرار نگیرد، هیچ تبلیغی نمایش داده نمی‌شود — و اگر کاربر یکی از سطح‌های حمایت
 * مالی را خریده باشد (تبلیغات حذف‌شده)، برای همیشه خاموش می‌ماند.
 *
 * اتصال SDK تپسل: docs/TAPSELL_INTEGRATION.md
 */
@Singleton
class TapsellAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy
) : AdManager {

    override val enabled: Boolean get() = AdConfig.enabled

    override suspend fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val info = cm.activeNetworkInfo ?: return false
        return info.isConnectedOrConnecting
    }

    override suspend fun onDrawCompleted() {
        frequencyPolicy.onDrawCompleted()
    }

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!enabled || SupportStore.tier.adsRemoved) return false
        if (!isNetworkAvailable()) return false
        if (!frequencyPolicy.shouldShowInterstitial()) return false
        // TODO(Tapsell): نمایش بین‌صفحه‌ای — docs/TAPSELL_INTEGRATION.md
        return false
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!enabled || SupportStore.tier.adsRemoved) return false
        if (!isNetworkAvailable()) return false
        // TODO(Tapsell): نمایش ویدئوی پاداشی — docs/TAPSELL_INTEGRATION.md
        return false
    }
}
