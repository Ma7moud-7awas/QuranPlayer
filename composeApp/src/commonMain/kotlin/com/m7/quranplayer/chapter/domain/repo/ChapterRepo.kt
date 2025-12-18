package com.m7.quranplayer.chapter.domain.repo

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.downloader.domain.model.DownloadState

interface ChapterRepo {

    suspend fun getChapters(downloadState: (String) -> DownloadState): List<Chapter>
}