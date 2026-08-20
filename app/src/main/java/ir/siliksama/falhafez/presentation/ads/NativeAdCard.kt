package ir.siliksama.falhafez.presentation.ads

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.R
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.ntv.NativeAdViewContainer

/** تبلیغ همسان تپسل — داخل فهرست دیوان، هم‌شکل با کارت‌های اپ. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) return
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val container = NativeAdViewContainer(ctx)
            LayoutInflater.from(ctx).inflate(R.layout.ad_native, container, true)
            runCatching {
                Tapsell.requestNativeAd(AdConfig.ZONE_NATIVE, object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        val act = ctx.findActivity()
                        if (act != null && !act.isFinishing) {
                            Tapsell.showNativeAd(adId, container, act, object : AdStateListener.Native {
                                override fun onAdImpression() {}
                                override fun onAdClicked() {}
                                override fun onAdClosed(completionState: AdShowCompletionState) {}
                                override fun onAdFailed(message: String) {}
                            })
                        }
                    }
                    override fun onFailure(message: String) {}
                })
            }
            container
        }
    )
}
