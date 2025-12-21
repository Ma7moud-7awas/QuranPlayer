package com.m7.quranplayer.downloader.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

@Immutable
sealed interface DownloadState {

    data object NotDownloaded : DownloadState

    data object Queued : DownloadState

    @Immutable
    data class Paused(val downloadedPercent: Float) : DownloadState

    data object Completed : DownloadState

    @Immutable
    data class Downloading(val updatedPosition: Flow<Float>) : DownloadState

    @Immutable
    data class Error(val error: Exception?) : DownloadState
}