package com.m7.quranplayer.ads.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.m7.quranplayer.core.AdBannerContainer

@Composable
actual fun AdBanner(adWidth: Int, modifier: Modifier) {
    UIKitView(
        modifier = modifier.size(width = adWidth.dp, height = 50.dp),
        factory = {
            AdBannerContainer(adWidth).view
        }
    )
}