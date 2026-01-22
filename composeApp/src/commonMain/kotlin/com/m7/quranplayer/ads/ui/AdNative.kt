package com.m7.quranplayer.ads.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m7.quranplayer.ads.AdState

expect fun loadAd(onAdStateChanged:(AdState) -> Unit)

@Composable
expect fun AdNative(modifier: Modifier = Modifier)