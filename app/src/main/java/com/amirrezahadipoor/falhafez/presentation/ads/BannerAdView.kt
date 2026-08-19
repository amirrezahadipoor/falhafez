package com.amirrezahadipoor.falhafez.presentation.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.amirrezahadipoor.falhafez.data.ads.AdConfig

/** Non-intrusive banner. Shown only on Home (niyyat), History and Library. */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().wrapContentHeight(), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConfig.BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
