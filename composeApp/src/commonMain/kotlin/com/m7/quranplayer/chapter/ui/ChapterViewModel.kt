package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.model.DownloaderAction
import com.m7.quranplayer.downloader.domain.repo.DownloaderRepo
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.domain.repo.PlayerRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.allStringResources
import quranplayer.composeapp.generated.resources.chapter_number

class ChapterViewModel(
    private val chapterRepo: ChapterRepo,
    private val playerRepo: PlayerRepo,
    private val downloaderRepo: DownloaderRepo,
) : ViewModel() {

    fun onLanguageChanged() {
        viewModelScope.launch {
            delay(50)
            buildChapters()
        }
    }

    private val originalChapters = MutableStateFlow<List<Chapter>>(emptyList())

    val chapters: StateFlow<List<Chapter>>
        field = MutableStateFlow(emptyList())

    init {
        buildChapters()

        viewModelScope.launch(Dispatchers.Default) {
            // route media center actions
            playerRepo.playerAction.collectLatest { playerAction(it) }

            // update download state
            downloaderRepo.downloadState.collect { (downloadId, state) ->
                if (state == DownloadState.NotDownloaded
                    || state is DownloadState.Paused
                    || state is DownloadState.Error
                ) {
                    downloadedAllEnabled = true
                }

                viewModelScope.launch(Dispatchers.Default) {
                    originalChapters.updateAndGet {
                        it.map { chapter ->
                            if (chapter.id == downloadId)
                                chapter.copy(downloadState = state) else chapter
                        }
                    }.also { newChapters ->
                        chapters.update { newChapters }
                    }

                    updateDownloadedCount()
                }
            }
        }
    }

    private fun buildChapters() {
        viewModelScope.launch(Dispatchers.Default) {
            originalChapters.updateAndGet {
                chapterRepo.getChapters(
                    getChapterTitle = {
                        getString(
                            Res.allStringResources["_${it}"] ?: Res.string.chapter_number
                        )
                    },
                    getChapterDownloadState = {
                        downloaderRepo.getDownloadState(it)
                    }
                )
            }.also { newChapters ->
                chapters.update { newChapters }
            }

            updateDownloadedCount()
        }
    }

    fun search(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            text
                .takeIf { it.isNotBlank() }
                ?.trim()
                ?.also { searchName ->
                    originalChapters.value
                        .filter {
                            it.title.contains(searchName, ignoreCase = true)
                        }.also { searchedChapters ->
                            chapters.update {
                                // reset player
                                setSelectedIndex(-1)
                                searchedChapters
                            }
                        }
                } ?: chapters.update {
                // reset player
                setSelectedIndex(-1)
                originalChapters.value
            }
        }
    }

    var downloadedChaptersCount by mutableIntStateOf(0)
        private set

    var downloadedAllEnabled by mutableStateOf(downloadedChaptersCount < 114)
        private set

    private suspend fun updateDownloadedCount() {
        downloadedChaptersCount = downloaderRepo.getDownloadedCount()
    }

    fun downloadAll(start: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            if (start) {
                downloadedAllEnabled = false
                originalChapters.value.forEach { (id, _, _, downloadState) ->
                    if (downloadState == DownloadState.NotDownloaded
                        || downloadState is DownloadState.Paused
                    ) {
                        downloaderRepo.start(id)
                    }
                }
            } else {
                downloadedAllEnabled = true
                originalChapters.value.forEach { (id, _, _, downloadState) ->
                    if (downloadState is DownloadState.Downloading
                        || downloadState == DownloadState.Queued
                    ) {
                        downloaderRepo.pause(id)
                    }
                }
            }
        }
    }

    var selectedChapterIndx by mutableIntStateOf(-1)
        private set

    fun setSelectedIndex(indx: Int) {
        when (indx) {
            -1 -> {
                // deselect state.
                selectedChapterIndx = indx
                playerAction(PlayerAction.Pause)
            }

            selectedChapterIndx if playerState.value.second is PlayerState.Playing -> {
                // pause the same chapter.
                playerAction(PlayerAction.Pause)
            }

            else -> {
                // play a new chapter.
                selectedChapterIndx = indx
                playerAction(PlayerAction.Play)
            }
        }
    }

    val playerState: StateFlow<Pair<String?, PlayerState>> = playerRepo.playerState
        .onEach { (id, state) ->
            id?.also {
                selectedChapterIndx = chapters.value.indexOfFirst { it.id == id }
            }

            if (state is PlayerState.Ended) {
                if (isRepeatEnabled) {
                    playerAction(PlayerAction.Repeat)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null to PlayerState.Idle
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

                is PlayerAction.Next,
                is PlayerAction.Next.WithId -> next()

                is PlayerAction.Previous,
                is PlayerAction.Previous.WithId -> previous()

                is PlayerAction.Repeat -> playerRepo.repeat()
                is PlayerAction.SeekTo -> playerRepo.seekTo(action.positionMs)
            }
        }
    }

    private fun play() {
        viewModelScope.launch {
            if (selectedChapterIndx > -1) {
                chapters.value.subList(selectedChapterIndx, chapters.value.size).let {
                    playerRepo.play(it)
                }
            }
        }
    }

    private fun previous() {
        viewModelScope.launch {
            if (selectedChapterIndx > 0) {
                selectedChapterIndx--
                chapters.value.subList(selectedChapterIndx, chapters.value.size).let {
                    playerRepo.previous(it)
                }
            }
        }
    }

    private fun next() {
        viewModelScope.launch {
            if (selectedChapterIndx < chapters.value.lastIndex) {
                selectedChapterIndx++
                playerRepo.next()
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