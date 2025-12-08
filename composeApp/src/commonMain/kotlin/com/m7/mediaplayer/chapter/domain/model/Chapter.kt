package com.m7.mediaplayer.chapter.domain.model

import mediaplayer.composeapp.generated.resources.Res
import mediaplayer.composeapp.generated.resources.allStringResources
import org.jetbrains.compose.resources.StringResource

data class Chapter(
    // used as a path to the audio file in the request
    val id: String,
    // localized representation of the id
    val number: String,
    // used to retrieve localized chapter title from resources
    val title: StringResource? = Res.allStringResources["_${id}"]
) {
    // used to represent the chapter number in the UI
    val trimmedNumber = number.trimStart { it == '0'  || it == '٠'}
}