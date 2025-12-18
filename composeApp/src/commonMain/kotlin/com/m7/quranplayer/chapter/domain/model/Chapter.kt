package com.m7.quranplayer.chapter.domain.model

import com.m7.quranplayer.core.di.format
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.downloader.domain.model.DownloadState
import org.jetbrains.compose.resources.StringResource
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.allStringResources
import quranplayer.composeapp.generated.resources.chapter_number

data class Chapter constructor(
    // used as a path to the audio file in the request
    val id: String,
    // localized representation of the id as chapter number
    val number: String,
    // used to retrieve localized chapter title from resources
    val titleRes: StringResource = Res.allStringResources["_${id}"] ?: Res.string.chapter_number,

    var downloadState: DownloadState
) {

    companion object Builder {
        var intId: Int = 0
            set(value) {
                field = value
                id = value.format("%03d")
                number = value.localize()
            }

        var id: String = ""
        var number: String = ""
        var downloadState: DownloadState = DownloadState.NotDownloaded

        fun setId(id: Int): Builder {
            this.intId = id
            return Builder
        }

        fun setDownloadState(downloadState: (String) -> DownloadState): Builder {
            this.downloadState = downloadState(id)
            return Builder
        }

        fun build(): Chapter {
            return Chapter(
                id = id,
                number = number,
                downloadState = downloadState
            )
        }
    }
}