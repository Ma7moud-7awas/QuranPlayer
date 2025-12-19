package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.coroutines.channels.Channel

interface DownloaderSource {

    val downloadState: Channel<Pair<String, DownloadState>>

    suspend fun getDownloadState(id: String): DownloadState

    suspend fun getDownloadedCount(): Int

    suspend fun start(id: String, url: String)

    suspend fun pause(id: String)

    suspend fun stop(id: String)
}