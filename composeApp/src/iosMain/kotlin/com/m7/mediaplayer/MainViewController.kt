package com.m7.mediaplayer

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() =
    ComposeUIViewController {
        KoinKt.doInitKoin()
        App()
    }