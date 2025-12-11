package com.m7.quranplayer.chapter.data

import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.chapter.domain.model.Chapter

class ChaptersRepoImpl : ChapterRepo {

    companion object {
        val chaptersList by lazy {
            buildList {
                for (i in 1..114) {
                    add(Chapter.build(i))
                }
            }
        }
    }

    override suspend fun getChapters(): List<Chapter> {
        return chaptersList
    }
}