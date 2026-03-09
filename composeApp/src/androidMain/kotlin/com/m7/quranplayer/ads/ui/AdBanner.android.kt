package com.m7.quranplayer.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

const val BannerAdId = "ca-app-pub-3940256099942544/9214589741"

@Composable
actual fun AdBanner(adWidth: Int, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                adUnitId = BannerAdId
                setAdSize(
                    AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}