package com.m7.quranplayer.ads.ui

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.m7.quranplayer.ads.AdState
import org.koin.java.KoinJavaComponent.inject

class AdHolder (private val context: Context) {
    companion object Companion {
        val instance :AdHolder by inject(AdHolder::class.java)
    }

    var nativeAd: NativeAd? = null

    fun loadNativeAd(updateAdState: (AdState) -> Unit) {
        updateAdState(AdState.Loading)
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->
                this.nativeAd = nativeAd
                updateAdState(AdState.Success)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    updateAdState(AdState.Failed)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}