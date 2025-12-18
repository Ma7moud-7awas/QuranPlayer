package com.m7.quranplayer.chapter.data

import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlin.collections.buildList

class ChaptersRepoImpl : ChapterRepo {

    companion object {
        val chaptersList by lazy {
            buildList {
                for (i in 1..12) {
                    add(Chapter.build(i) { DownloadState.NotDownloaded })
                }
            }
        }
    }

    override suspend fun getChapters(downloadState: (String) -> DownloadState): List<Chapter> {
        val chaptersList = buildList {
            for (i in 1..114) {
                add(
                    Chapter.build(i, downloadState)
                )
            }
        }

        return chaptersList
    }
}