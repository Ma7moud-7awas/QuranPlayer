package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChapterViewModel(
    private val chapterRepo: ChapterRepo,
    private val playerRepo: PlayerRepo,
) : ViewModel() {

    val chapters: StateFlow<List<Chapter>>
        field = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch {
            chapters.update { chapterRepo.getChapters() }
        }
    }

    val playerState: StateFlow<PlayerState> = playerRepo.playerState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState.Idle
        )

    var selectedChapterIndx by mutableIntStateOf(0)
        private set

    fun setSelectedIndex(indx: Int) {
        selectedChapterIndx = indx
        playerAction(PlayerAction.Play)
    }

    fun playerAction(action: PlayerAction) {
        when (action) {
            PlayerAction.Pause -> playerRepo.pause()
            is PlayerAction.Play -> play()
            is PlayerAction.Next -> next()
            is PlayerAction.Previous -> previous()
            is PlayerAction.Repeat -> playerRepo.repeat()
            is PlayerAction.SeekTo -> playerRepo.seekTo(action.positionMs)
        }
    }

    private fun play() {
        if (selectedChapterIndx == -1) {
            // todo: play last cashed chapter
            selectedChapterIndx++
        }
        playerRepo.play(chapters.value[selectedChapterIndx].id)
    }

    private fun next() {
        if (selectedChapterIndx < chapters.value.lastIndex) {
            selectedChapterIndx++
            playerRepo.play(chapters.value[selectedChapterIndx].id)
        }
    }

    private fun previous() {
        if (selectedChapterIndx > 0) {
            selectedChapterIndx--
            playerRepo.play(chapters.value[selectedChapterIndx].id)
        }
    }

    override fun onCleared() {
        playerRepo.release()
        super.onCleared()
    }
}