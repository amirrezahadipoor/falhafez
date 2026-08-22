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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private const val TAG = "FalHafezAds"
private const val MAX_REQUEST_ATTEMPTS = 3

/**
 * شبکهٔ تبلیغاتی تپسل (Mediation SDK).
 *
 * نکات کلیدیِ درست‌شده نسبت به نسخهٔ قبل:
 *  1. هیچ درخواستی پیش از آماده‌شدنِ SDK فرستاده نمی‌شود ([TapsellInit.await]) —
 *     علتِ اصلیِ «تبلیغ نمایش داده نمی‌شود» همین بود.
 *  2. preload به‌جای سازنده، در [warmUp] انجام می‌شود؛ یعنی **بعد از** خوانده‌شدنِ
 *     سطحِ حمایت، تا برای مشترکان هیچ درخواستی نرود.
 *  3. تلاشِ مجدد (retry با backoff) واقعاً کار می‌کند.
 */
@Singleton
class TapsellAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var cachedInterstitialId: String? = null
    @Volatile private var cachedRewardedId: String? = null
    @Volatile private var warmedUp = false

    /**
     * آخرین پیامِ شکست از تپ‌سل — برای صفحهٔ عیب‌یابیِ تنظیمات.
     * بدونِ این، تنها راهِ فهمیدنِ علت وصل‌کردنِ گوشی به adb بود.
     */
    @Volatile var lastError: String? = null
        private set

    /**
     * ⚠️ `AdConfig.enabled` نیست، `tapsellEnabled`.
     *
     * `AdConfig.enabled` وقتی هم `true` است که فقط ادیوری پیکربندی شده باشد؛
     * اگر اینجا از آن استفاده می‌کردیم، آبشار تپ‌سل را «فعال» می‌دید، هر بار
     * ۸ ثانیه پشتِ `TapsellInit.await()` منتظر می‌ماند و تازه بعد سراغِ ادیوری
     * می‌رفت — یعنی کاربر برای هر تبلیغ ۸ ثانیه تأخیرِ بی‌دلیل می‌دید.
     */
    override val enabled: Boolean get() = AdConfig.tapsellEnabled

    /** تبلیغات فقط وقتی معنا دارد که: پیکربندی درست باشد و کاربر مشترک نباشد. */
    private val adsAllowed: Boolean
        get() = enabled && !SupportStore.tier.adsRemoved

    /**
     * گرم‌کردنِ کَش.
     *
     * ⚠️ باگی که اینجا رفع شد: پیش‌تر `warmedUp = true` **پیش از** بررسیِ شبکه
     * ست می‌شد. اگر اپ در لحظهٔ شروع آفلاین بود (خیلی رایج: کاربر اپ را باز
     * می‌کند بعد اینترنت وصل می‌شود)، این پرچم برای همیشه true می‌ماند و
     * preload **هرگز** دوباره تلاش نمی‌کرد — یعنی تا بسته‌شدنِ کاملِ اپ هیچ
     * تبلیغِ آماده‌ای وجود نداشت.
     *
     * حالا پرچم فقط پس از یک تلاشِ **واقعی** ست می‌شود.
     */
    override fun warmUp() {
        if (warmedUp || !adsAllowed) return
        scope.launch {
            TapsellInit.await()
            if (!isNetworkAvailable()) {
                // پرچم را ست نمی‌کنیم تا وقتی اینترنت آمد دوباره تلاش شود.
                Log.d(TAG, "warmUp skipped: offline (will retry later)")
                return@launch
            }
            if (!adsAllowed) return@launch
            warmedUp = true
            preloadInterstitial()
            preloadRewarded()
        }
    }

    /**
     * دوباره تلاش کن اگر گرم‌کردنِ اولیه به‌خاطر آفلاین‌بودن انجام نشده.
     * از `HomeViewModel.refreshQuota()` صدا زده می‌شود، یعنی هر بار که اپ به
     * پیش‌زمینه برمی‌گردد — دقیقاً همان‌جا که وضعیتِ شبکه ممکن است عوض شده باشد.
     */
    override fun retryWarmUpIfNeeded() {
        if (!warmedUp) warmUp()
    }

    private suspend fun preloadInterstitial() {
        if (!adsAllowed || cachedInterstitialId != null) return
        val adId = request("interstitial-preload") {
            Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, it)
        }
        if (adId != null) {
            cachedInterstitialId = adId
            Log.d(TAG, "interstitial preloaded ✓ adId=$adId")
        }
    }

    private suspend fun preloadRewarded() {
        if (!adsAllowed || cachedRewardedId != null) return
        val adId = request("rewarded-preload") {
            Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, it)
        }
        if (adId != null) {
            cachedRewardedId = adId
            Log.d(TAG, "rewarded preloaded ✓ adId=$adId")
        }
    }

    override suspend fun isNetworkAvailable(): Boolean = isOnline(context)

    override suspend fun onDrawCompleted() {
        frequencyPolicy.onDrawCompleted()
    }

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!adsAllowed) {
            Log.d(TAG, "interstitial skipped: ads not allowed (tier=${SupportStore.tier})")
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

        TapsellInit.await()

        val adId = cachedInterstitialId?.also { cachedInterstitialId = null }
            // overloadِ دارایِ Activity — بدونِ آن آداپترهای مدیشن پاسخ نمی‌دهند.
            ?: request("interstitial") {
                Tapsell.requestInterstitialAd(AdConfig.ZONE_INTERSTITIAL, activity, it)
            }

        if (adId == null) {
            Log.w(TAG, "interstitial: no ad available")
            scope.launch { preloadInterstitial() }
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

        if (shown) frequencyPolicy.recordShown()
        scope.launch { preloadInterstitial() }
        return shown
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        if (!adsAllowed) {
            Log.d(TAG, "rewarded skipped: ads not allowed (tier=${SupportStore.tier})")
            return false
        }
        if (!isNetworkAvailable()) {
            Log.d(TAG, "rewarded skipped: offline")
            return false
        }

        TapsellInit.await()

        val adId = cachedRewardedId?.also { cachedRewardedId = null }
            // overloadِ دارایِ Activity — بدونِ آن آداپترهای مدیشن پاسخ نمی‌دهند.
            ?: request("rewarded") {
                Tapsell.requestRewardedAd(AdConfig.ZONE_REWARDED, activity, it)
            }

        if (adId == null) {
            Log.w(TAG, "rewarded: no ad available")
            scope.launch { preloadRewarded() }
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

        scope.launch { preloadRewarded() }
        return shown
    }

    /**
     * درخواستِ تبلیغ با تلاشِ مجدد و backoff.
     * (نسخهٔ قبل حلقهٔ retry داشت ولی هرگز خودش را دوباره صدا نمی‌زد.)
     */
    private suspend fun request(
        kind: String,
        call: (RequestResultListener) -> Unit
    ): String? {
        repeat(MAX_REQUEST_ATTEMPTS) { attempt ->
            val id = requestOnce(kind, attempt, call)
            if (id != null) return id
            if (attempt < MAX_REQUEST_ATTEMPTS - 1) {
                delay(1_500L * (attempt + 1))
            }
        }
        Log.w(TAG, "$kind: giving up after $MAX_REQUEST_ATTEMPTS attempts")
        return null
    }

    private suspend fun requestOnce(
        kind: String,
        attempt: Int,
        call: (RequestResultListener) -> Unit
    ): String? = suspendCancellableCoroutine { cont ->
        runCatching {
            call(object : RequestResultListener {
                override fun onSuccess(adId: String) {
                    Log.d(TAG, "$kind: adId=$adId (attempt ${attempt + 1})")
                    if (cont.isActive) cont.resume(adId)
                }

                override fun onFailure(message: String) {
                    Log.w(TAG, "$kind failed (attempt ${attempt + 1}/$MAX_REQUEST_ATTEMPTS): $message")
                    lastError = "$kind: $message"
                    if (cont.isActive) cont.resume(null)
                }
            })
        }.getOrElse {
            Log.w(TAG, "$kind request threw", it)
            if (cont.isActive) cont.resume(null)
        }
    }
}
