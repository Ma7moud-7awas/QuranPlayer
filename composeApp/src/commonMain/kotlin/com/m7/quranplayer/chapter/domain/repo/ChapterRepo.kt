package com.m7.quranplayer.chapter.domain.repo

import com.m7.quranplayer.chapter.domain.model.Chapter

interface ChapterRepo {

    suspend fun getChapters(): List<Chapter>
}