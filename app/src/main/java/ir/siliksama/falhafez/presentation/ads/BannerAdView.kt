package ir.siliksama.falhafez.presentation.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.siliksama.falhafez.data.ads.isOnline
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer

private const val TAG = "FalHafezAds"
private const val MAX_ATTEMPTS = 3
private const val RETRY_MS = 5000L

/** بنر استاندارد تپسل (320x50) — غیرمزاحم، فقط در صفحات آرام. */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) {
        Log.w(TAG, "banner skipped: AdConfig.enabled=false")
        return
    }
    if (SupportStore.tier.adsRemoved) {
        Log.d(TAG, "banner skipped: ads removed (tier=${SupportStore.tier})")
        return
    }
    Box(modifier = modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val container = BannerContainer(ctx)
                requestBannerWithRetry(ctx, container, attempt = 0)
                container
            }
        )
    }
}

private fun requestBannerWithRetry(ctx: Context, container: BannerContainer, attempt: Int) {
    if (attempt >= MAX_ATTEMPTS) {
        Log.w(TAG, "banner: giving up after $MAX_ATTEMPTS attempts")
        return
    }
    if (!isOnline(ctx)) {
        Log.d(TAG, "banner: offline — request skipped")
        return
    }
    runCatching {
        Tapsell.requestBannerAd(
            AdConfig.ZONE_BANNER,
            BannerSize.BANNER_320_50,
            object : RequestResultListener {
                override fun onSuccess(adId: String) {
                    val act = ctx.findActivity()
                    if (act != null && !act.isFinishing) {
                        Log.d(TAG, "banner: adId=$adId — showing")
                        Tapsell.showBannerAd(adId, container, act, object : AdStateListener.Banner {
                            override fun onAdImpression() { Log.d(TAG, "banner: impression ✓") }
                            override fun onAdClicked() { Log.d(TAG, "banner: clicked") }
                            override fun onAdFailed(message: String) {
                                Log.w(TAG, "banner show failed: $message")
                            }
                        })
                    } else {
                        Log.w(TAG, "banner: no activity at show time — retrying")
                        retryBanner(ctx, container, attempt + 1)
                    }
                }

                override fun onFailure(message: String) {
                    // رایج‌ترین دلیل: اپ/زون هنوز در پنل تپسل تأیید نشده، یا در منطقهٔ هدف نیست.
                    Log.w(TAG, "banner request failed (attempt ${attempt + 1}/$MAX_ATTEMPTS): $message")
                    retryBanner(ctx, container, attempt + 1)
                }
            }
        )
    }.onFailure { Log.w(TAG, "banner request threw", it) }
}

private fun retryBanner(ctx: Context, container: BannerContainer, attempt: Int) {
    Handler(Looper.getMainLooper()).postDelayed(
        { requestBannerWithRetry(ctx, container, attempt) },
        RETRY_MS
    )
}
