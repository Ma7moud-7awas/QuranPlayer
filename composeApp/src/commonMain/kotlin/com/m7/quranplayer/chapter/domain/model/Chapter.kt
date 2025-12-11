package com.m7.quranplayer.chapter.domain.model

import com.m7.quranplayer.core.di.format
import com.m7.quranplayer.core.di.localize
import org.jetbrains.compose.resources.StringResource
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.allStringResources
import quranplayer.composeapp.generated.resources.chapter_number

@ConsistentCopyVisibility
data class Chapter private constructor(
    // used as a path to the audio file in the request
    val id: String,
    // localized representation of the id as chapter number
    val number: String,
    // used to retrieve localized chapter title from resources
    val titleRes: StringResource = Res.allStringResources["_${id}"] ?: Res.string.chapter_number
) {
    companion object Builder {
        fun build(id: Int): Chapter {
            return Chapter(
                id = id.format("%03d"),
                number = id.localize()
            )
        }
    }
}