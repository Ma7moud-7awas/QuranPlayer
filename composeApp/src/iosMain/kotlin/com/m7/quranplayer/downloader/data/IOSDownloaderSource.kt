package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.Log.log
import com.m7.quranplayer.downloader.data.model.Download
import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDownloadTask
import platform.Foundation.NSURLSessionTaskStateCanceling
import platform.Foundation.NSURLSessionTaskStateCompleted
import platform.Foundation.NSURLSessionTaskStateRunning
import platform.Foundation.NSURLSessionTaskStateSuspended
import platform.Foundation.downloadTaskWithResumeData
import platform.Foundation.downloadTaskWithURL

@OptIn(ExperimentalForeignApi::class)
class IOSDownloaderSource(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DownloaderSource {

    override val downloadState: Channel<Pair<String, DownloadState>> = Channel(CONFLATED)

    val session = NSURLSession.sharedSession
    var downloads: MutableMap<String, Download> = mutableMapOf()

    override suspend fun getDownloadState(id: String): DownloadState {
        return downloads[id]?.state
            ?: DownloadManager.getFileDownloadState(id)
                .also {
                    // add download to local map if it was already persisted
                    if (it != DownloadState.NotDownloaded)
                        downloads[id] = Download(id = id, state = it)
                }
    }

    private fun NSURLSessionDownloadTask.getDownloadState(id: String): DownloadState {
        return when (state) {
            NSURLSessionTaskStateSuspended -> {
                Log("download: id = $id - state = $state Suspended")
                DownloadState.Paused(progress.fractionCompleted.toFloat())
            }

            NSURLSessionTaskStateRunning -> {
                Log("download: id = $id - state = $state Running")
                DownloadState.Downloading(
                    flow {
                        while (progress.fractionCompleted < 100) {
                            delay(50)
                            emit(progress.fractionCompleted.toFloat())
                        }
                    }
                )
            }

            NSURLSessionTaskStateCanceling -> {
                Log("download: id = $id - state = $state Canceling")
                DownloadState.Error(Exception(error?.localizedDescription))
            }

            NSURLSessionTaskStateCompleted -> {
                Log("download: id = $id - state = $state Completed")
                DownloadState.Completed
            }

            else -> {
                Log("download: id = $id - state = $state else")
                DownloadState.NotDownloaded
            }
        }
    }

    override suspend fun getDownloadedCount(): Int {
        return downloads.keys.count { DownloadManager.checkDownloadExists(it) }
    }

    override suspend fun start(id: String, url: String) {
        downloads[id]?.let {
            // resume paused download
            if (it.state is DownloadState.Paused) {
                DownloadManager.getDownloadData(id)?.let { resumeData ->
                    it.task = session.downloadTaskWithResumeData(resumeData, completionHandler(id))
                    it.state = DownloadState.Queued
                }
            } else null
        } ?: run {
            // start a new download
            val nsUrl = NSURL.URLWithString(url) ?: return
            downloads[id] = Download(
                id = id,
                state = DownloadState.Queued,
                task = session.downloadTaskWithURL(nsUrl, completionHandler(id))
            )
        }

        downloadState.trySend(id to DownloadState.Queued)

        runQueuedTasks()
    }

    private fun completionHandler(id: String): (NSURL?, NSURLResponse?, NSError?) -> Unit {
        return handler@{ localUrl, _, error ->
            // check if it's a failure
            error?.also {
                it.log()

                if (it.code != -999L) { // !cancelled
                    updateDownloadState(
                        id, DownloadState.Error(Exception(it.toString()))
                    )
                }
                return@handler
            }

            // move the downloaded file to documents directory
            try {
                DownloadManager.moveDownload(id, localUrl!!)
                updateDownloadState(id, DownloadState.Completed)
            } catch (e: Exception) {
                updateDownloadState(id, DownloadState.Error(e))
            } finally {
                runQueuedTasks()
            }
        }
    }

    private fun runQueuedTasks() {
        val runningTasksCount = downloads.values.count {
            it.task?.state == NSURLSessionTaskStateRunning
        }
        if (runningTasksCount < 3) {
            downloads.values.firstOrNull { it.state == DownloadState.Queued }
                ?.also {
                    runTask(it.id)
                }
        }
    }

    private fun runTask(id: String) {
        scope.launch {
            // fire
            downloads[id]?.task?.resume()
            // fetch updates
            while (downloads[id]?.task?.state == NSURLSessionTaskStateRunning) {
                downloads[id]?.also {
                    if (it.state !is DownloadState.Downloading) {
                        it.task?.also {
                            updateDownloadState(id, it.getDownloadState(id))
                        }
                    }
                }

                delay(1000)
            }
        }
    }

    private fun updateDownloadState(id: String, state: DownloadState) {
        downloads[id]?.let {
            // update local map
            it.state = state
            // notify the UI
            downloadState.trySend(id to state)
        }
    }

    override suspend fun pause(id: String) {
        downloads[id]?.task?.also {
            it.cancelByProducingResumeData { data ->
                it.progress.fractionCompleted.toFloat().also { progress ->
                    // store the data
                    data?.also { DownloadManager.saveDownloadData(id, data, progress) }

                    updateDownloadState(id, DownloadState.Paused((progress)))
                }
            }
        }
    }

    override suspend fun stop(id: String) {
        // remove and cancel download
        downloads.remove(id)
            ?.task?.cancel()
        // remove persisted download/directory
        DownloadManager.let {
            it.removeDownload(id)
            it.removeDownload(id, it.getPausedDownloadDirectoryUrl(id))
        }
        // update state
        downloadState.trySend(id to DownloadState.NotDownloaded)
    }
}