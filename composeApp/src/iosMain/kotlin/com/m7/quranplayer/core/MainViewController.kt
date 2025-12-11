package com.m7.quranplayer.core

import androidx.compose.ui.window.ComposeUIViewController
import com.m7.quranplayer.core.ui.App
import com.m7.quranplayer.core.di.initKoin

fun MainViewController() =
    ComposeUIViewController {
        initKoin()
        App()
    }