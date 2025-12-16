package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.coroutines.channels.Channel

interface DownloaderSource {

    val downloadState: Channel<Pair<String, DownloadState>>

    fun getDownloadState(id: String): DownloadState

    fun start(id: String, url: String)

    fun pause(id: String)

    fun stop(id: String)
}