package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.downloader.domain.model.DownloaderAction
import com.m7.quranplayer.downloader.domain.repo.DownloaderRepo
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.domain.repo.PlayerRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChapterViewModel(
    private val chapterRepo: ChapterRepo,
    private val playerRepo: PlayerRepo,
    private val downloaderRepo: DownloaderRepo,
) : ViewModel() {

    val chapters: StateFlow<List<Chapter>>
        field = MutableStateFlow(emptyList())

    var downloadedChaptersCount by mutableIntStateOf(0)
        private set

    init {
        viewModelScope.launch(Dispatchers.Default) {
            chapters.update {
                chapterRepo.getChapters().map { chapter ->
                    chapter.copy(downloadState = downloaderRepo.getDownloadState(chapter.id))
                }
            }

            updateDownloadedCount()
        }

        viewModelScope.launch(Dispatchers.Default) {
            downloaderRepo.downloadState.collect { (downloadId, state) ->
                chapters.update {
                    it.map { chapter ->
                        if (chapter.id == downloadId) {
                            chapter.copy(downloadState = state)
                        } else {
                            chapter
                        }
                    }
                }

                updateDownloadedCount()
            }
        }
    }

    private fun updateDownloadedCount() {
        viewModelScope.launch {
            downloadedChaptersCount = downloaderRepo.getDownloadedCount()
        }
    }

    var selectedChapterIndx by mutableIntStateOf(-1)
        private set

    fun setSelectedIndex(indx: Int) {
        // pause the same chapter or play the new one.
        if (indx == selectedChapterIndx && playerState.value is PlayerState.Playing) {
            playerAction(PlayerAction.Pause)
        } else {
            selectedChapterIndx = indx
            playerAction(PlayerAction.Play)
        }
    }

    // player
    val playerState: StateFlow<PlayerState> = playerRepo.playerState
        .onEach {
            if (it is PlayerState.Ended)
                if (isRepeatEnabled) {
                    playerAction(PlayerAction.Repeat)
                } else {
                    playerAction(PlayerAction.Next)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerState.Idle
        )

    var isRepeatEnabled by mutableStateOf(false)
        private set

    fun onRepeatClicked(enabled: Boolean) {
        isRepeatEnabled = enabled
    }

    fun playerAction(action: PlayerAction) {
        viewModelScope.launch {
            when (action) {
                PlayerAction.Pause -> playerRepo.pause()
                is PlayerAction.Play -> play()
                is PlayerAction.Next -> next()
                is PlayerAction.Previous -> previous()
                is PlayerAction.Repeat -> playerRepo.repeat()
                is PlayerAction.SeekTo -> playerRepo.seekTo(action.positionMs)
            }
        }
    }

    private fun play() {
        viewModelScope.launch {
            if (selectedChapterIndx == -1) {
                // todo: play last cashed chapter, or scroll to the bookmarked chapter
                selectedChapterIndx++
            }
            playerRepo.play(chapters.value[selectedChapterIndx].id)
        }
    }

    private fun next() {
        viewModelScope.launch {
            if (selectedChapterIndx < chapters.value.lastIndex) {
                selectedChapterIndx++
                playerRepo.play(chapters.value[selectedChapterIndx].id)
            }
        }
    }

    private fun previous() {
        viewModelScope.launch {
            if (selectedChapterIndx > 0) {
                selectedChapterIndx--
                playerRepo.play(chapters.value[selectedChapterIndx].id)
            }
        }
    }

    fun downloaderAction(id: String, action: DownloaderAction) {
        viewModelScope.launch {
            when (action) {
                is DownloaderAction.Start -> downloaderRepo.start(id)
                is DownloaderAction.Pause -> downloaderRepo.pause(id)
                is DownloaderAction.Stop -> downloaderRepo.stop(id)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            playerRepo.release()
        }
        super.onCleared()
    }
}