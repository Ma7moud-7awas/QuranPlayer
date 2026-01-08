package com.m7.quranplayer.chapter.domain.model

import com.m7.quranplayer.core.di.format
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.downloader.domain.model.DownloadState

data class Chapter(
    // used as a path to the audio file in the request.
    val id: String,
    // localized representation of the id as chapter number.
    val number: String,
    // localized chapter title.
    val title: String,
    // title retriever from a special font, it should be used as a title string directly.
    val titleByFontCode: String,
    // runtime download state holder.
    val downloadState: DownloadState = DownloadState.NotDownloaded
) {

    companion object Builder {
        suspend fun build(
            id: Int,
            title: String,
            titleFontCode: String,
            getDownloadState: suspend (fileId: String) -> DownloadState
        ): Chapter {
            val formattedId = id.format("%03d")
            return Chapter(
                id = formattedId,
                number = id.localize(),
                title = title,
                titleByFontCode = titleFontCode,
                downloadState = getDownloadState(formattedId)
            )
        }
    }
}