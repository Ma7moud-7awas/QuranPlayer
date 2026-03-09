package com.m7.quranplayer.downloader.data.model

import com.m7.quranplayer.downloader.domain.model.DownloadState
import platform.Foundation.NSURLSessionDownloadTask

data class Download(
    val id: String,
    val state: DownloadState,
    val task: NSURLSessionDownloadTask? = null,
)