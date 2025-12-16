package com.m7.quranplayer.downloader.domain.repo

import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloaderRepo {

    val downloadState: Flow<Pair<String, DownloadState>>

    fun getDownloadState(id: String): DownloadState

    fun start(id: String)

    fun pause(id: String)

    fun stop(id: String)
}