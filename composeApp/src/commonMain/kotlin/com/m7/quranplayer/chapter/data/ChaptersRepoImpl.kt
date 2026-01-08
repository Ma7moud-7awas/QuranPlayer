package com.m7.quranplayer.chapter.data

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.downloader.domain.model.DownloadState
import org.jetbrains.compose.resources.getStringArray
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.allStringArrayResources

class ChaptersRepoImpl : ChapterRepo {

    override suspend fun getChapters(
        getChapterDownloadState: suspend (String) -> DownloadState
    ): List<Chapter> {
        val chaptersNames = Res.allStringArrayResources["chapters_names"]
            ?.let { getStringArray(it) }
            ?: throw IllegalStateException("'chapters_names' string-array not found!")

        val chaptersNamesFontCodes = try {
            Res.allStringArrayResources["chapters_font_codes"]
                ?.let { getStringArray(it) }
                ?: chaptersNames
        } catch (_: IllegalStateException) {
            chaptersNames
        }

        return buildList {
            for (i in 1..114) {
                add(
                    Chapter.build(
                        id = i,
                        title = chaptersNames[i],
                        titleFontCode = chaptersNamesFontCodes[i],
                        getDownloadState = getChapterDownloadState
                    )
                )
            }
        }
    }
}