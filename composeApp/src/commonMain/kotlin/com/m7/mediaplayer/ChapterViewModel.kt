package com.m7.mediaplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.mediaplayer.model.Chapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val chaptersPlaceholder = listOf(
    Chapter(1, "Al Fatiha", 100, ""),
    Chapter(2, "Al Bakara", 1200, ""),
    Chapter(3, "A'l Emran", 30, ""),
    Chapter(
        4,
        "Al Nesaa",
        10,
        "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"
    ),
)

class ChapterViewModel(private val player: Player) : ViewModel() {

    val loading: StateFlow<Boolean>
        field = MutableStateFlow(true)

    val chapters: StateFlow<List<Chapter>>
        field = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            delay(2000)

            loading.update { false }
            chapters.update { chaptersPlaceholder }
        }
    }

    fun play(chapter: Chapter) {
        player.play(chapter.url)
    }
}
