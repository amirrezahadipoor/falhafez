package ir.siliksama.falhafez.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "FalHafezAds"

/**
 * پیاده‌سازیِ [AdManager] روی ادیوری (سرویسِ نمایشِ یکتانت).
 *
 * ### تفاوتِ مدلِ ادیوری با تپ‌سل
 * تپ‌سل «درخواست کن → adId بگیر → نشان بده» است. ادیوری اما **حافظه‌محور** است:
 * `prepareXAd(context, placement)` تبلیغ را در حافظه آماده می‌کند، `isLoaded()`
 * می‌گوید آماده هست یا نه، و `showAd(placement)` نمایش می‌دهد. رویدادها از راهِ
 * یک [AdiveryListener] سراسری می‌آیند، نه callbackِ همان درخواست.
 *
 * به همین دلیل اینجا یک listener ثبت می‌کنیم و نتیجه را با
 * [suspendCancellableCoroutine] به دنیای coroutine پل می‌زنیم.
 */
@Singleton
class AdiveryAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy,
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile private var warmedUp = false

    /** آخرین پیامِ خطا — برای کارتِ عیب‌یابیِ تنظیمات. */
    @Volatile var lastError: String? = null
        private set

    /** منتظرانِ بارگذاری، به تفکیکِ placement. */
    private val loadWaiters = java.util.concurrent.ConcurrentHashMap<String, MutableList<(Boolean) -> Unit>>()

    /** آیا کاربر جایزه را کامل گرفت؟ بینِ show و onRewardedAdClosed نگه داشته می‌شود. */
    @Volatile private var rewardGranted = false

    /** listenerِ سراسری که همهٔ رویدادهای ادیوری از آن می‌گذرد. */
    private val listener = object : AdiveryListener() {
        override fun onInterstitialAdLoaded(placementId: String) {
            Log.d(TAG, "adivery: interstitial loaded ✓ ($placementId)")
            notifyLoad(placementId, true)
        }

        override fun onRewardedAdLoaded(placementId: String) {
            Log.d(TAG, "adivery: rewarded loaded ✓ ($placementId)")
            notifyLoad(placementId, true)
        }

        override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
            Log.d(TAG, "adivery: rewarded closed (rewarded=$isRewarded)")
            rewardGranted = isRewarded
        }

        override fun log(placementId: String, message: String) {
            // ادیوری خطاها را از همین مسیر می‌دهد.
            Log.d(TAG, "adivery[$placementId]: $message")
            if (message.contains("error", true) || message.contains("fail", true)) {
                lastError = "adivery/$placementId: $message"
                notifyLoad(placementId, false)
            }
        }
    }

    private fun notifyLoad(placement: String, success: Boolean) {
        val waiters = loadWaiters.remove(placement) ?: return
        waiters.forEach { runCatching { it(success) } }
    }

    private val adsAllowed: Boolean
        get() = AdConfig.adiveryEnabled &&
            !ir.siliksama.falhafez.core.util.SupportStore.tier.adsRemoved

    override val enabled: Boolean get() = AdConfig.adiveryEnabled

    override suspend fun isNetworkAvailable(): Boolean = isOnline(context)

    override fun warmUp() {
        if (!adsAllowed) return
        scope.launch {
            if (!isNetworkAvailable()) {
                Log.d(TAG, "adivery: warmUp skipped — offline")
                return@launch
            }
            if (!AdiveryInit.isReady) {
                Log.d(TAG, "adivery: warmUp skipped — sdk not configured")
                return@launch
            }
            warmedUp = true
            runCatching {
                Adivery.addGlobalListener(listener)
                if (AdConfig.ADIVERY_INTERSTITIAL.isNotBlank()) {
                    Adivery.prepareInterstitialAd(context, AdConfig.ADIVERY_INTERSTITIAL)
                }
                if (AdConfig.ADIVERY_REWARDED.isNotBlank()) {
                    Adivery.prepareRewardedAd(context, AdConfig.ADIVERY_REWARDED)
                }
            }.onFailure { Log.w(TAG, "adivery: warmUp failed", it) }
        }
    }

    override fun retryWarmUpIfNeeded() {
        if (!warmedUp) warmUp()
    }

    /**
     * منتظرِ آماده‌شدنِ یک placement می‌ماند. اگر همین حالا آماده باشد فوراً
     * برمی‌گردد؛ وگرنه `prepare` می‌زند و تا [timeoutMs] صبر می‌کند.
     */
    private suspend fun awaitLoaded(placement: String, prepare: () -> Unit, timeoutMs: Long = 6_000): Boolean {
        if (placement.isBlank() || !AdiveryInit.isReady) return false
        if (AdiveryInit.isLoaded(placement)) return true

        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                loadWaiters.getOrPut(placement) { mutableListOf() }.add { ok ->
                    if (cont.isActive) cont.resume(ok)
                }
                runCatching { prepare() }.onFailure {
                    Log.w(TAG, "adivery: prepare threw for $placement", it)
                    if (cont.isActive) cont.resume(false)
                }
            }
        } ?: false
    }

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!adsAllowed) return false
        val placement = AdConfig.ADIVERY_INTERSTITIAL
        if (placement.isBlank()) return false
        if (!isNetworkAvailable()) return false
        if (!frequencyPolicy.shouldShowInterstitial()) {
            Log.d(TAG, "adivery: interstitial skipped — frequency cap")
            return false
        }

        val ready = awaitLoaded(placement) {
            Adivery.prepareInterstitialAd(context, placement)
        }
        if (!ready) {
            Log.w(TAG, "adivery: no interstitial available")
            return false
        }

        val shown = withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching { Adivery.showAd(placement) }.isSuccess
        }
        if (shown) {
            frequencyPolicy.recordShown()
            // تبلیغِ بعدی را برای دفعهٔ بعد آماده کن.
            scope.launch { runCatching { Adivery.prepareInterstitialAd(context, placement) } }
        }
        return shown
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!adsAllowed) return false
        val placement = AdConfig.ADIVERY_REWARDED
        if (placement.isBlank()) return false
        if (!isNetworkAvailable()) return false

        val ready = awaitLoaded(placement) {
            Adivery.prepareRewardedAd(context, placement)
        }
        if (!ready) {
            Log.w(TAG, "adivery: no rewarded available")
            return false
        }

        rewardGranted = false
        val shown = withContext(Dispatchers.Main) {
            if (activity.isFinishing || activity.isDestroyed) return@withContext false
            runCatching { Adivery.showAd(placement) }.isSuccess
        }
        if (!shown) return false

        // تا بسته‌شدنِ تبلیغ صبر می‌کنیم؛ onRewardedAdClosed پرچم را ست می‌کند.
        val granted = withTimeoutOrNull(180_000) {
            while (!rewardGranted) kotlinx.coroutines.delay(250)
            true
        } ?: false

        if (granted) onReward()
        scope.launch { runCatching { Adivery.prepareRewardedAd(context, placement) } }
        return true
    }

    override suspend fun onDrawCompleted() {
        frequencyPolicy.onDrawCompleted()
    }
}
