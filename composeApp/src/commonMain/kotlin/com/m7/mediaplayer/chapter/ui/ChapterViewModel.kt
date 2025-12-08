package com.m7.mediaplayer.chapter.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.mediaplayer.chapter.domain.repo.ChapterRepo
import com.m7.mediaplayer.chapter.domain.repo.PlayerRepo
import com.m7.mediaplayer.chapter.domain.model.Chapter
import com.m7.mediaplayer.chapter.domain.model.PlayerAction
import com.m7.mediaplayer.chapter.domain.model.PlayerState
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

    var selectedChapterIndx by mutableIntStateOf(0)
        private set

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

    @Stable
    fun setSelectedIndex(indx: Int) {
        selectedChapterIndx = indx
        playerAction(PlayerAction.Play)
    }

    @Stable
    fun playerAction(action: PlayerAction) {
        with(playerRepo) {
            when (action) {
                PlayerAction.Pause -> pause()

                is PlayerAction.Play -> {
                    if (selectedChapterIndx == -1) {
//                         todo: play last cashed chapter
                        selectedChapterIndx++
                    }
                    play(chapters.value[selectedChapterIndx].id)
                }

                is PlayerAction.Next -> {
                    if (selectedChapterIndx < chapters.value.lastIndex) {
                        selectedChapterIndx++
                        play(chapters.value[selectedChapterIndx].id)
                    }
                }

                is PlayerAction.Previous -> {
                    if (selectedChapterIndx > 0) {
                        selectedChapterIndx--
                        play(chapters.value[selectedChapterIndx].id)
                    }
                }

                is PlayerAction.SeekTo -> seekTo(action.positionMs)
            }
        }
    }

    override fun onCleared() {
        playerRepo.release()
        super.onCleared()
    }
}