package com.m7.quranplayer.chapter.domain.repo

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.downloader.domain.model.DownloadState

interface ChapterRepo {

    suspend fun getChapters(
        getChapterTitle: suspend (String) -> String,
        getChapterDownloadState: suspend (String) -> DownloadState
    ): List<Chapter>
}