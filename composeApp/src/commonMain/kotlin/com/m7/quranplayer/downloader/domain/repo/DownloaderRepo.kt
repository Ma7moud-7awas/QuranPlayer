package com.m7.quranplayer.downloader.domain.repo

import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloaderRepo {

    val downloadState: Flow<Pair<String, DownloadState>>

    suspend fun getDownloadState(id: String): DownloadState

    suspend fun getDownloadedCount(): Int

    suspend fun start(id: String)

    suspend fun pause(id: String)

    suspend fun stop(id: String)
}