package com.m7.quranplayer.downloader.data.model

import com.m7.quranplayer.downloader.domain.model.DownloadState
import platform.Foundation.NSURLSessionDownloadTask

data class Download(
    val id: String,
    var state: DownloadState,
    var task: NSURLSessionDownloadTask? = null,
)