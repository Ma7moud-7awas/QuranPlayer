package com.m7.quranplayer.chapter.data

import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlin.collections.buildList

class ChaptersRepoImpl : ChapterRepo {

    override suspend fun getChapters(
        getChapterTitle: suspend (String) -> String,
        getChapterDownloadState: suspend (String) -> DownloadState
    ): List<Chapter> {
        return buildList {
            for (i in 1..114) {
                add(Chapter.build(i, getChapterTitle, getChapterDownloadState))
            }
        }
    }
}