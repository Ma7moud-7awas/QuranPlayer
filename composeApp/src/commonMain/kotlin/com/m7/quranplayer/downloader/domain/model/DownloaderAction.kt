package com.m7.quranplayer.downloader.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DownloaderAction {

    data object Start : DownloaderAction

    data object Pause : DownloaderAction

    data object Stop : DownloaderAction
}