package com.m7.quranplayer.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.m7.quranplayer.ads.AdState
import com.m7.quranplayer.core.AdNativeContainer
import com.m7.quranplayer.core.loadNativeAd

actual fun loadAd(onAdStateChanged: (AdState) -> Unit) {
    loadNativeAd(onAdStateChanged)
}

@Composable
actual fun AdNative(modifier: Modifier) {
    UIKitView(
        modifier = modifier,
        factory = {
            AdNativeContainer().view
        }
    )
}