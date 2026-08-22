package ir.siliksama.falhafez.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
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

private const val TAG = "FalHafezAds"

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

    override suspend fun isNetworkAvailable(): Boolean = isOnline(context)

    override suspend fun onDrawCompleted() {
        frequencyPolicy.onDrawCompleted()
    }

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!enabled) {
            Log.w(TAG, "interstitial skipped: AdConfig.enabled=false")
            return false
        }
        if (SupportStore.tier.adsRemoved) {
            Log.d(TAG, "interstitial skipped: ads removed (tier=${SupportStore.tier})")
            return false
        }
        if (!isNetworkAvailable()) {
            Log.d(TAG, "interstitial skipped: offline")
            return false
        }
        if (!frequencyPolicy.shouldShowInterstitial()) {
            Log.d(TAG, "interstitial skipped: frequency cap not reached")
            return false
        }

        val adId = request("interstitial") { Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, it) } ?: return false

        val shown = withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching {
                Tapsell.showInterstitialAd(adId, activity, object : AdStateListener.Interstitial {
                    override fun onAdImpression() { Log.d(TAG, "interstitial: impression ✓") }
                    override fun onAdClicked() {}
                    override fun onAdClosed(completionState: AdShowCompletionState) {}
                    override fun onAdFailed(message: String) { Log.w(TAG, "interstitial show failed: $message") }
                })
            }.isSuccess
        }
        if (shown) frequencyPolicy.recordShown()
        return shown
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!enabled) {
            Log.w(TAG, "rewarded skipped: AdConfig.enabled=false")
            return false
        }
        if (SupportStore.tier.adsRemoved) {
            Log.d(TAG, "rewarded skipped: ads removed (tier=${SupportStore.tier})")
            return false
        }
        if (!isNetworkAvailable()) {
            Log.d(TAG, "rewarded skipped: offline")
            return false
        }

        val adId = request("rewarded") { Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, it) } ?: return false

        return withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching {
                Tapsell.showRewardedAd(adId, activity, object : AdStateListener.Rewarded {
                    override fun onAdImpression() { Log.d(TAG, "rewarded: impression ✓") }
                    override fun onAdClicked() {}
                    override fun onAdClosed(completionState: AdShowCompletionState) {}
                    override fun onAdFailed(message: String) { Log.w(TAG, "rewarded show failed: $message") }
                    override fun onRewarded() { onReward() }
                })
            }.isSuccess
        }
    }

    /** درخواست تبلیغ با zone و لاگِ علتِ شکست. */
    private suspend fun request(
        kind: String,
        call: (RequestResultListener) -> Unit
    ): String? = suspendCancellableCoroutine { cont ->
        runCatching {
            call(object : RequestResultListener {
                override fun onSuccess(adId: String) {
                    Log.d(TAG, "$kind: adId=$adId")
                    if (cont.isActive) cont.resume(adId)
                }

                override fun onFailure(message: String) {
                    // رایج‌ترین دلیل: اپ/زون هنوز در پنل تپسل تأیید نشده، منطقهٔ هدف، یا آفلاین.
                    Log.w(TAG, "$kind request failed: $message")
                    if (cont.isActive) cont.resume(null)
                }
            })
        }.getOrElse {
            Log.w(TAG, "$kind request threw", it)
            if (cont.isActive) cont.resume(null)
        }
    }
}
