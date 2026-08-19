package com.amirrezahadipoor.falhafez.presentation.ads

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.amirrezahadipoor.falhafez.R
import com.amirrezahadipoor.falhafez.data.ads.AdConfig
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/** One tastefully integrated native ad unit for the Divan Browser list. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.ad_native, null, false) as NativeAdView
            val loader = AdLoader.Builder(ctx, AdConfig.NATIVE_ID)
                .forNativeAd { ad -> populate(view, ad) }
                .build()
            loader.loadAd(AdRequest.Builder().build())
            view
        }
    )
}

private fun populate(view: NativeAdView, ad: NativeAd) {
    val headline = view.findViewById<TextView>(R.id.ad_headline)
    val body = view.findViewById<TextView>(R.id.ad_body)
    val icon = view.findViewById<ImageView>(R.id.ad_icon)
    val media = view.findViewById<MediaView>(R.id.ad_media)
    val cta = view.findViewById<Button>(R.id.ad_cta)

    view.headlineView = headline
    view.bodyView = body
    view.iconView = icon
    view.mediaView = media
    view.callToActionView = cta

    headline.text = ad.headline
    if (ad.body.isNullOrBlank()) {
        body.visibility = View.GONE
    } else {
        body.text = ad.body
    }

    val iconDrawable: Drawable? = ad.icon?.drawable
    if (iconDrawable != null) {
        icon.setImageDrawable(iconDrawable)
        icon.visibility = View.VISIBLE
    } else {
        icon.visibility = View.GONE
    }

    if (ad.mediaContent != null) {
        media.mediaContent = ad.mediaContent
        media.visibility = View.VISIBLE
    } else {
        media.visibility = View.GONE
    }

    if (ad.callToAction.isNullOrBlank()) {
        cta.visibility = View.GONE
    } else {
        cta.text = ad.callToAction
    }

    view.setNativeAd(ad)
}
