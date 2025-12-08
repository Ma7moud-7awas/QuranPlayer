package com.m7.mediaplayer.chapter.data

import com.m7.mediaplayer.chapter.domain.repo.ChapterRepo
import com.m7.mediaplayer.chapter.domain.model.Chapter
import com.m7.mediaplayer.core.di.format
import com.m7.mediaplayer.core.di.formatLocalized

class ChaptersRepoImpl : ChapterRepo {

    companion object {
        val chaptersList by lazy {
            buildList {
                for (i in 1..114) {
                    add(
                        Chapter(
                            id = i.format("%03d"),
                            number = i.formatLocalized("%03d")
                        )
                    )
                }
            }
        }
    }

    override suspend fun getChapters(): List<Chapter> {
        return chaptersList
    }
}