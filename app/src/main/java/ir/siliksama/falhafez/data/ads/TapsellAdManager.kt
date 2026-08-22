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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "FalHafezAds"

/**
 * شبکهٔ تبلیغاتی تپسل (Mediation SDK) — اصلی برای بازار ایران.
 *
 * پیش‌بارگذاری (Preloading) تبلیغات ویدیویی و بین‌صفحه‌ای انجام می‌شود
 * تا هنگام کلیک کاربر، تبلیغ به‌صورت آنی و بدون تاخیر نمایش داده شود.
 *
 * اگر کاربر یکی از سطح‌های حمایت مالی را خریده باشد (تبلیغات حذف‌شده)،
 * هیچ تبلیغی نمایش داده نمی‌شود — برای همیشه.
 */
@Singleton
class TapsellAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var cachedInterstitialId: String? = null

    @Volatile
    private var cachedRewardedId: String? = null

    @Volatile
    private var isInitializing = false

    override val enabled: Boolean get() = AdConfig.enabled

    init {
        ensureInitialized()
        if (enabled && !SupportStore.tier.adsRemoved) {
            preloadAds()
        }
    }

    private fun ensureInitialized() {
        if (isInitializing) return
        isInitializing = true
        runCatching {
            Tapsell.initialize(context, AdConfig.TAPSELL_APP_KEY)
            Log.d(TAG, "Tapsell initialized with key: ${AdConfig.TAPSELL_APP_KEY}")
        }.onFailure {
            Log.w(TAG, "Tapsell explicit initialization notice", it)
        }
    }

    private fun preloadAds() {
        scope.launch {
            if (isNetworkAvailable()) {
                if (cachedInterstitialId == null) preloadInterstitial()
                if (cachedRewardedId == null) preloadRewarded()
            }
        }
    }

    private suspend fun preloadInterstitial() {
        if (SupportStore.tier.adsRemoved) return
        val adId = request("interstitial-preload") { Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, it) }
        if (adId != null) {
            cachedInterstitialId = adId
            Log.d(TAG, "interstitial preloaded successfully: adId=$adId")
        }
    }

    private suspend fun preloadRewarded() {
        if (SupportStore.tier.adsRemoved) return
        val adId = request("rewarded-preload") { Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, it) }
        if (adId != null) {
            cachedRewardedId = adId
            Log.d(TAG, "rewarded preloaded successfully: adId=$adId")
        }
    }

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

        ensureInitialized()

        var adId = cachedInterstitialId
        if (adId != null) {
            cachedInterstitialId = null
            Log.d(TAG, "using preloaded interstitial adId=$adId")
        } else {
            Log.d(TAG, "no preloaded interstitial, requesting on-demand...")
            adId = request("interstitial") { Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, it) }
        }

        if (adId == null) {
            Log.w(TAG, "interstitial request/preload returned null adId")
            return false
        }

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

        if (shown) {
            frequencyPolicy.recordShown()
        }

        // Trigger next preload in background
        scope.launch { preloadInterstitial() }

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

        ensureInitialized()

        var adId = cachedRewardedId
        if (adId != null) {
            cachedRewardedId = null
            Log.d(TAG, "using preloaded rewarded adId=$adId")
        } else {
            Log.d(TAG, "no preloaded rewarded, requesting on-demand...")
            adId = request("rewarded") { Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, it) }
        }

        if (adId == null) {
            Log.w(TAG, "rewarded request/preload returned null adId")
            return false
        }

        val shown = withContext(Dispatchers.Main) {
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

        // Trigger next preload in background
        scope.launch { preloadRewarded() }

        return shown
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
