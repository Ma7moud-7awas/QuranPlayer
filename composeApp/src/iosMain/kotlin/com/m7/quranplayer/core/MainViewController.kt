package com.m7.quranplayer.core

import androidx.compose.ui.window.ComposeUIViewController
import com.m7.quranplayer.ads.AdState
import com.m7.quranplayer.core.di.initKoin
import com.m7.quranplayer.core.ui.App
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

lateinit var AdBannerContainer: (Int) -> UIViewController
lateinit var AdNativeContainer: () -> UIViewController
lateinit var loadNativeAd: (updateAdState: (AdState) -> Unit) -> Unit

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    initKoin()

    AVAudioSession.sharedInstance().apply {
        setCategory(AVAudioSessionCategoryPlayback, null)
        setActive(true, null)
    }

    App(onLanguageChanged = { changeLanguage(it) })
}

private fun changeLanguage(langCode: String) {
    NSUserDefaults.standardUserDefaults
        .setObject(listOf(langCode), "AppleLanguages")
}
