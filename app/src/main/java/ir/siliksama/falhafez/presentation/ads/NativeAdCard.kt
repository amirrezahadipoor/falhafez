package ir.siliksama.falhafez.presentation.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.util.SupportStore
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
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
            NativeAdViewContainer(ctx).also { container ->
                container.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                LayoutInflater.from(ctx).inflate(R.layout.ad_native, container, true)
                TapsellInit.whenReady { requestNativeWithRetry(ctx, container, 0) }
            }
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
                if (act != null && !act.isFinishing && !act.isDestroyed) {
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
                    Log.w(TAG, "native: no active activity at show time")
                }
            }

            override fun onFailure(message: String) {
                Log.w(TAG, "native request failed (attempt ${attempt + 1}/$MAX_ATTEMPTS): $message")
                Handler(Looper.getMainLooper()).postDelayed(
                    { requestNativeWithRetry(ctx, container, attempt + 1) },
                    2_000L * (attempt + 1)
                )
            }
        })
    }.onFailure { Log.w(TAG, "native request threw", it) }
}
