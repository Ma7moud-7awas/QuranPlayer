package com.m7.quranplayer.chapter.domain.model

import com.m7.quranplayer.core.di.format
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.downloader.domain.model.DownloadState

data class Chapter(
    // used as a path to the audio file in the request
    val id: String,
    // localized representation of the id as chapter number
    val number: String,
    // used to retrieve localized chapter title from resources
    val title: String,

    var downloadState: DownloadState = DownloadState.NotDownloaded
) {

    companion object Builder {
        suspend fun build(
            id: Int,
            getTitle: suspend (id: String) -> String,
            getDownloadState: suspend (id: String) -> DownloadState
        ): Chapter {
            val formattedId = id.format("%03d")
            return Chapter(
                id = formattedId,
                number = id.localize(),
                title = getTitle(formattedId),
                downloadState = getDownloadState(formattedId)
            )
        }
    }
}