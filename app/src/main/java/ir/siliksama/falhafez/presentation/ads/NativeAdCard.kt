package ir.siliksama.falhafez.presentation.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.util.findActivity
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryNativeAdView
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.siliksama.falhafez.data.ads.AdiveryInit
import ir.siliksama.falhafez.data.ads.TapsellInit
import ir.siliksama.falhafez.data.ads.isOnline
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer

private const val TAG = "FalHafezAds"
private const val MAX_ATTEMPTS = 3

/** تبلیغ همسان تپسل — داخل فهرست دیوان، هم‌شکل با کارت‌های اپ. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) return
    if (SupportStore.tier.adsRemoved) return

    AndroidView(
        // ارتفاعِ حداقلی — بدون آن ممکن است کانتینر ارتفاعِ صفر بگیرد و تبلیغ دیده نشود.
        modifier = modifier.fillMaxWidth().heightIn(min = 120.dp),
        factory = { ctx ->
            // یک میزبانِ خالی که هر دو شبکه می‌توانند داخلش بنشینند. بدونِ آن،
            // ریشهٔ ویو از نوعِ کانتینرِ تپ‌سل می‌شد و جایگزینی با ادیوری ممکن نبود.
            val host = FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            if (AdConfig.tapsellEnabled) {
                val container = LayoutInflater.from(ctx)
                    .inflate(R.layout.ad_native, host, false) as NativeAdViewContainer
                host.addView(container)
                TapsellInit.whenReady { requestNativeWithRetry(ctx, host, container, 0) }
            } else {
                showAdiveryNative(ctx, host)
            }
            host
        }
    )
}

/**
 * تبلیغِ همسانِ ادیوری — پلهٔ دومِ آبشار.
 *
 * `AdiveryNativeAdView` خودش هم ویو است و هم بارگذارنده: قالب را می‌گیرد،
 * درخواست می‌فرستد و ویوها را پر می‌کند. پس کافی است جایگزینِ کانتینرِ تپ‌سل شود.
 */
private fun showAdiveryNative(ctx: Context, host: ViewGroup) {
    val placement = AdConfig.ADIVERY_NATIVE
    if (placement.isBlank() || !AdiveryInit.isReady) {
        Log.d(TAG, "native: adivery not configured")
        return
    }
    if (!isOnline(ctx)) return

    runCatching {
        val view = AdiveryNativeAdView(ctx).apply {
            setNativeAdLayout(R.layout.ad_native_adivery)
            setPlacementId(placement)
            setListener(object : AdiveryAdListener() {
                override fun onAdLoaded() { Log.d(TAG, "native: adivery loaded ✓") }
                override fun onAdShown() { Log.d(TAG, "native: adivery impression ✓") }
                override fun onError(reason: String) {
                    Log.w(TAG, "native: adivery error: $reason")
                    // چیزی برای نمایش نیست — میزبان را جمع می‌کنیم تا فضای
                    // خالیِ بی‌دلیل در فهرست نماند.
                    Handler(Looper.getMainLooper()).post { host.removeAllViews() }
                }
            })
        }
        Handler(Looper.getMainLooper()).post {
            host.removeAllViews()
            host.addView(view)
            view.loadAd()
        }
    }.onFailure { Log.w(TAG, "native: adivery request threw", it) }
}

private fun requestNativeWithRetry(
    ctx: Context,
    host: ViewGroup,
    container: NativeAdViewContainer,
    attempt: Int,
) {
    if (attempt >= MAX_ATTEMPTS) {
        Log.w(TAG, "native: tapsell gave up after $MAX_ATTEMPTS attempts — falling back to adivery")
        showAdiveryNative(ctx, host)
        return
    }
    if (!isOnline(ctx)) {
        Log.d(TAG, "native: offline — request skipped")
        return
    }

    // ⚠️ overloadِ دارایِ Activity الزامی است — آداپترهای مدیشن بدونِ آن
    // بی‌صدا شکست می‌خورند.
    val activity = ctx.findActivity()
    if (activity == null || activity.isFinishing || activity.isDestroyed) {
        Log.w(TAG, "native: no usable activity — request skipped")
        return
    }

    runCatching {
        Tapsell.requestNativeAd(AdConfig.ZONE_NATIVE, activity, object : RequestResultListener {
            override fun onSuccess(adId: String) {
                val act = ctx.findActivity()
                if (act != null && !act.isFinishing && !act.isDestroyed) {
                    Log.d(TAG, "native: adId=$adId — showing")
                    Tapsell.showNativeAd(adId, container, act, object : AdStateListener.Native {
                        override fun onAdImpression() { Log.d(TAG, "native: impression ✓") }
                        override fun onAdClicked() { Log.d(TAG, "native: clicked") }
                        override fun onAdClosed(completionState: AdShowCompletionState) {}
                        override fun onAdFailed(message: String) {
                            Log.w(TAG, "native show failed: $message — falling back to adivery")
                            showAdiveryNative(ctx, host)
                        }
                    })
                } else {
                    Log.w(TAG, "native: no active activity at show time")
                }
            }

            override fun onFailure(message: String) {
                Log.w(TAG, "native request failed (attempt ${attempt + 1}/$MAX_ATTEMPTS): $message")
                Handler(Looper.getMainLooper()).postDelayed(
                    { requestNativeWithRetry(ctx, host, container, attempt + 1) },
                    2_000L * (attempt + 1)
                )
            }
        })
    }.onFailure { Log.w(TAG, "native request threw", it) }
}
