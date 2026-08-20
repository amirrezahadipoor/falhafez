package ir.siliksama.falhafez.presentation.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ir.siliksama.falhafez.core.util.findActivity
import ir.siliksama.falhafez.data.ads.AdConfig
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import androidx.compose.foundation.layout.Box

/** بنر استاندارد تپسل (320x50) — غیرمزاحم، فقط در صفحات آرام. */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (!AdConfig.enabled) return
    Box(modifier = modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val container = BannerContainer(ctx)
                runCatching {
                    Tapsell.requestBannerAd(
                        AdConfig.ZONE_BANNER,
                        BannerSize.BANNER_320_50,
                        object : RequestResultListener {
                            override fun onSuccess(adId: String) {
                                val act = ctx.findActivity()
                                if (act != null && !act.isFinishing) {
                                    Tapsell.showBannerAd(adId, container, act, object : AdStateListener.Banner {
                                        override fun onAdImpression() {}
                                        override fun onAdClicked() {}
                                        override fun onAdFailed(message: String) {}
                                    })
                                }
                            }
                            override fun onFailure(message: String) {}
                        }
                    )
                }
                container
            }
        )
    }
}
