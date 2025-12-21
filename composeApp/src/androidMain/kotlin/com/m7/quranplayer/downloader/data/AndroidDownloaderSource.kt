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
        // check and resume queued downloads.
        DownloadService.start(context, DownloaderService::class.java)

        // track current downloads states.
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
                            request.id to this.getDownloadState(request.id)
                        )
                    }
                }
            }
        )
    }

    override suspend fun getDownloadState(id: String): DownloadState {
        return downloadUtil.getDownload(id)?.getDownloadState(id) ?: DownloadState.NotDownloaded
    }

    private fun Download.getDownloadState(id: String): DownloadState {
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
            Download.STATE_STOPPED -> DownloadState.Paused(percentDownloaded / 100)
            Download.STATE_FAILED -> DownloadState.Error(Exception(failureReason.toString()))
            else -> DownloadState.NotDownloaded
        }
    }

    override suspend fun getDownloadedCount(): Int {
        return downloadUtil.getDownloadManager()
            .downloadIndex
            .getDownloads(Download.STATE_COMPLETED)
            .count
    }

    override suspend fun start(id: String, url: String) {
        val downloadRequest = DownloadRequest.Builder(id, url.toUri()).build()

        DownloadService.sendAddDownload(
            context,
            DownloaderService::class.java,
            downloadRequest,
            /* foreground= */ true
        )
    }

    override suspend fun pause(id: String) {
        DownloadService.sendSetStopReason(
            context,
            DownloaderService::class.java,
            id,
            STOP_REASON_PAUSED,
            true
        )
    }

    override suspend fun stop(id: String) {
        DownloadService.sendRemoveDownload(
            context,
            DownloaderService::class.java,
            id,
            true
        )
    }
}