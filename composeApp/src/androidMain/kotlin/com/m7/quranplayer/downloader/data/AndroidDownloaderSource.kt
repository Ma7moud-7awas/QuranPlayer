package com.m7.quranplayer.downloader.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.downloader.DownloadUtil
import com.m7.quranplayer.downloader.DownloaderService
import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

const val STOP_REASON_PAUSED = 1

@OptIn(UnstableApi::class)
class AndroidDownloaderSource(
    private val context: Context,
    private val downloadUtil: DownloadUtil
) : DownloaderSource {

    override val downloadState: Channel<Pair<String, DownloadState>> = Channel(CONFLATED)

    init {
        downloadUtil.getDownloadManager().addListener(
            object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    Log("download.state = ${download.state}")
                    with(download) {
                        downloadState.trySend(
                            request.id to getDownloaderState(request.id)
                        )
                    }
                }
            }
        )
    }

    override fun getDownloadState(id: String): DownloadState {
        return downloadUtil.getDownload(id)?.getDownloaderState(id) ?: DownloadState.NotDownloaded
    }

    private fun Download.getDownloaderState(id: String): DownloadState {
        Log("download: id = $id - state = $state")
        return when (this.state) {
            Download.STATE_QUEUED -> DownloadState.Queued
            Download.STATE_DOWNLOADING -> DownloadState.Downloading(
                flow {
                    while (bytesDownloaded < contentLength) {
                        delay(50)
                        emit(percentDownloaded / 100)
                    }
                }
            )

            Download.STATE_COMPLETED -> DownloadState.Completed
            Download.STATE_STOPPED -> DownloadState.Paused
            Download.STATE_FAILED -> DownloadState.Error(Exception(failureReason.toString()))
            else -> DownloadState.NotDownloaded
        }
    }

    override fun start(id: String, url: String) {
        if (getDownloadState(id) !is DownloadState.Completed) {
            val downloadRequest = DownloadRequest.Builder(id, url.toUri()).build()
            DownloadService.sendAddDownload(
                context,
                DownloaderService::class.java,
                downloadRequest,
                /* foreground= */ true
            )
        } else {
            Log("$id already downloaded!")
        }
    }

    override fun pause(id: String) {
        DownloadService.sendSetStopReason(
            context,
            DownloaderService::class.java,
            id,
            STOP_REASON_PAUSED,
            true
        )
    }

    override fun stop(id: String) {
        DownloadService.sendRemoveDownload(
            context,
            DownloaderService::class.java,
            id,
            true
        )
    }
}