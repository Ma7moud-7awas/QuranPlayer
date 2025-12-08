package com.m7.mediaplayer.chapter.domain.repo

import com.m7.mediaplayer.chapter.domain.model.Chapter

interface ChapterRepo {

    suspend fun getChapters(): List<Chapter>
}