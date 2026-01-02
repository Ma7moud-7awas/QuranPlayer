package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.core.data.Url
import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.repo.DownloaderRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class DownloaderRepoImpl(
    private val downloaderSource: DownloaderSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DownloaderRepo {

    override val downloadState: Flow<Pair<String, DownloadState>> =
        downloaderSource.downloadState.receiveAsFlow()

    override suspend fun getDownloadState(id: String): DownloadState {
        return withContext(dispatcher) {
            downloaderSource.getDownloadState(id)
        }
    }

    override suspend fun getDownloadedCount(): Int {
        return withContext(dispatcher) {
            downloaderSource.getDownloadedCount()
        }
    }

    override suspend fun start(id: String) {
        withContext(dispatcher) {
            downloaderSource.start(id, Url.getDownloadUrlById(id))
        }
    }

    override suspend fun pause(id: String) {
        withContext(dispatcher) {
            downloaderSource.pause(id)
        }
    }

    override suspend fun stop(id: String) {
        withContext(dispatcher) {
            downloaderSource.stop(id)
        }
    }
}