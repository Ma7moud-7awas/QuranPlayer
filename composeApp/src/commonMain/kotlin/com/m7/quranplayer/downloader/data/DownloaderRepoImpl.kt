package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.repo.DownloaderRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class DownloaderRepoImpl(
    private val downloaderSource: DownloaderSource
) : DownloaderRepo {

    private companion object {
        // ex: https://server7.mp3quran.net/s_gmd/001.mp3
        private const val BASE_URL = "https://server7.mp3quran.net/s_gmd/"
        private const val EXTENSION = ".mp3"

        fun mapIdToUrl(id: String): String {
            return BASE_URL + id + EXTENSION
        }
    }

    override val downloadState: Flow<Pair<String, DownloadState>> =
        downloaderSource.downloadState.receiveAsFlow()

    override fun getDownloadState(id: String): DownloadState {
        return downloaderSource.getDownloadState(id)
    }

    override fun start(id: String) {
        downloaderSource.start(id, mapIdToUrl(id))
    }

    override fun pause(id: String) {
        downloaderSource.pause(id)
    }

    override fun stop(id: String) {
        downloaderSource.stop(id)
    }
}