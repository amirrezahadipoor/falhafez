package ir.siliksama.falhafez.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import ir.siliksama.falhafez.core.util.SupportStore
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * شبکهٔ تبلیغاتی تپسل (Mediation SDK) — اصلی برای بازار ایران.
 * SDK به‌صورت خودکار با کلیدِ موجود در مانیفست راه‌اندازی می‌شود (auto-init).
 *
 * اگر کاربر یکی از سطح‌های حمایت مالی را خریده باشد (تبلیغات حذف‌شده)،
 * هیچ تبلیغی نمایش داده نمی‌شود — برای همیشه.
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

        val adId = request { Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, it) } ?: return false

        val shown = withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching {
                Tapsell.showInterstitialAd(adId, activity, object : AdStateListener.Interstitial {
                    override fun onAdImpression() {}
                    override fun onAdClicked() {}
                    override fun onAdClosed(completionState: AdShowCompletionState) {}
                    override fun onAdFailed(message: String) {}
                })
            }.isSuccess
        }
        if (shown) frequencyPolicy.recordShown()
        return shown
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!enabled || SupportStore.tier.adsRemoved) return false
        if (!isNetworkAvailable()) return false

        val adId = request { Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, it) } ?: return false

        return withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching {
                Tapsell.showRewardedAd(adId, activity, object : AdStateListener.Rewarded {
                    override fun onAdImpression() {}
                    override fun onAdClicked() {}
                    override fun onAdClosed(completionState: AdShowCompletionState) {}
                    override fun onAdFailed(message: String) {}
                    override fun onRewarded() { onReward() }
                })
            }.isSuccess
        }
    }

    /** درخواست تبلیغ با zone (zone خالی = تلاش برای zone پیش‌فرض). */
    private suspend fun request(
        call: (RequestResultListener) -> Unit
    ): String? = suspendCancellableCoroutine { cont ->
        runCatching {
            call(object : RequestResultListener {
                override fun onSuccess(adId: String) {
                    if (cont.isActive) cont.resume(adId)
                }
                override fun onFailure(message: String) {
                    if (cont.isActive) cont.resume(null)
                }
            })
        }.getOrElse {
            if (cont.isActive) cont.resume(null)
        }
    }
}
