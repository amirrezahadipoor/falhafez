package ir.siliksama.falhafez.presentation.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.siliksama.falhafez.data.ads.isOnline
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer

private const val TAG = "FalHafezAds"
private const val MAX_ATTEMPTS = 3
private const val RETRY_MS = 5000L

/** تبلیغ همسان تپسل — داخل فهرست دیوان، هم‌شکل با کارت‌های اپ. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) {
        Log.w(TAG, "native skipped: AdConfig.enabled=false")
        return
    }
    if (SupportStore.tier.adsRemoved) {
        Log.d(TAG, "native skipped: ads removed (tier=${SupportStore.tier})")
        return
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val container = NativeAdViewContainer(ctx)
            LayoutInflater.from(ctx).inflate(R.layout.ad_native, container, true)
            requestNativeWithRetry(ctx, container, attempt = 0)
            container
        }
    )
}

private fun requestNativeWithRetry(ctx: Context, container: NativeAdViewContainer, attempt: Int) {
    if (attempt >= MAX_ATTEMPTS) {
        Log.w(TAG, "native: giving up after $MAX_ATTEMPTS attempts")
        return
    }
    if (!isOnline(ctx)) {
        Log.d(TAG, "native: offline — request skipped")
        return
    }
    runCatching {
        Tapsell.requestNativeAd(AdConfig.ZONE_NATIVE, object : RequestResultListener {
            override fun onSuccess(adId: String) {
                val act = ctx.findActivity()
                if (act != null && !act.isFinishing) {
                    Log.d(TAG, "native: adId=$adId — showing")
                    Tapsell.showNativeAd(adId, container, act, object : AdStateListener.Native {
                        override fun onAdImpression() { Log.d(TAG, "native: impression ✓") }
                        override fun onAdClicked() { Log.d(TAG, "native: clicked") }
                        override fun onAdClosed(completionState: AdShowCompletionState) {}
                        override fun onAdFailed(message: String) {
                            Log.w(TAG, "native show failed: $message")
                        }
                    })
                } else {
                    Log.w(TAG, "native: no activity at show time — retrying")
                    retryNative(ctx, container, attempt + 1)
                }
            }

            override fun onFailure(message: String) {
                Log.w(TAG, "native request failed (attempt ${attempt + 1}/$MAX_ATTEMPTS): $message")
                retryNative(ctx, container, attempt + 1)
            }
        })
    }.onFailure { Log.w(TAG, "native request threw", it) }
}

private fun retryNative(ctx: Context, container: NativeAdViewContainer, attempt: Int) {
    Handler(Looper.getMainLooper()).postDelayed(
        { requestNativeWithRetry(ctx, container, attempt) },
        RETRY_MS
    )
}
