package ir.siliksama.falhafez.presentation.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.siliksama.falhafez.data.ads.TapsellInit
import ir.siliksama.falhafez.data.ads.isOnline
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer

private const val TAG = "FalHafezAds"
private const val MAX_ATTEMPTS = 3

/**
 * بنر استاندارد تپسل (320x50) — غیرمزاحم، فقط در صفحاتِ آرام.
 *
 * درست‌شده‌ها نسبت به نسخهٔ قبل:
 *  - درخواست پشتِ [TapsellInit] صف می‌شود (قبلاً پیش از آماده‌شدنِ SDK می‌رفت و شکست می‌خورد).
 *  - `destroyBannerAd` در [DisposableEffect] — جلوگیری از نشتِ حافظه با هر تعویضِ تب.
 *  - تلاشِ مجدد واقعاً اجرا می‌شود.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) return
    if (SupportStore.tier.adsRemoved) return

    // adId را نگه می‌داریم تا هنگام خروج، destroy شود.
    val adIdHolder = remember { arrayOfNulls<String>(1) }

    DisposableEffect(Unit) {
        onDispose {
            adIdHolder[0]?.let { id ->
                runCatching { Tapsell.destroyBannerAd(id) }
                    .onSuccess { Log.d(TAG, "banner destroyed: $id") }
            }
            adIdHolder[0] = null
        }
    }

    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                BannerContainer(ctx).also { container ->
                    TapsellInit.whenReady {
                        requestBannerWithRetry(ctx, container, 0) { id -> adIdHolder[0] = id }
                    }
                }
            }
        )
    }
}

private fun requestBannerWithRetry(
    ctx: Context,
    container: BannerContainer,
    attempt: Int,
    onLoaded: (String) -> Unit
) {
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
                    onLoaded(adId)
                    val act = ctx.findActivity()
                    if (act != null && !act.isFinishing && !act.isDestroyed) {
                        Log.d(TAG, "banner: adId=$adId — showing")
                        Tapsell.showBannerAd(adId, container, act, object : AdStateListener.Banner {
                            override fun onAdImpression() { Log.d(TAG, "banner: impression ✓") }
                            override fun onAdClicked() { Log.d(TAG, "banner: clicked") }
                            override fun onAdFailed(message: String) {
                                Log.w(TAG, "banner show failed: $message")
                            }
                        })
                    } else {
                        Log.w(TAG, "banner: no active activity at show time")
                    }
                }

                override fun onFailure(message: String) {
                    Log.w(TAG, "banner request failed (attempt ${attempt + 1}/$MAX_ATTEMPTS): $message")
                    // تلاشِ مجدد با backoff — این بخش قبلاً وجود نداشت.
                    Handler(Looper.getMainLooper()).postDelayed(
                        { requestBannerWithRetry(ctx, container, attempt + 1, onLoaded) },
                        2_000L * (attempt + 1)
                    )
                }
            }
        )
    }.onFailure { Log.w(TAG, "banner request threw", it) }
}
