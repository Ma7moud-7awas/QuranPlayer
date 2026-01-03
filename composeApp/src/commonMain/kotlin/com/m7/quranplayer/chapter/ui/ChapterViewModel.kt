package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.core.Log
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

        // route media center actions
        viewModelScope.launch(Dispatchers.Default) {
            playerRepo.playerAction.collectLatest { playerAction(it) }
        }

        // observe & update download state
        viewModelScope.launch(Dispatchers.Default) {
            downloaderRepo.downloadState.collect { (downloadId, state) ->
                Log("downloadId= $downloadId - state= $state")
                if (state == DownloadState.NotDownloaded
                    || state is DownloadState.Paused
                    || state is DownloadState.Error
                ) {
                    downloadedAllEnabled = true
                }

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
                updateChapters(newChapters)
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
                            updateChapters(searchedChapters)
                        }
                } ?: updateChapters(originalChapters.value)
        }
    }

    private suspend fun updateChapters(newChapters: List<Chapter>) {
        chapters.update { newChapters }
        playerRepo.setPlaylist(newChapters)
        setSelectedIndex(-1)
    }

    var selectedChapterIndx by mutableIntStateOf(-1)
        private set

    fun setSelectedIndex(indx: Int) {
        when (indx) {
            -1 -> {
                // deselect state. "reset"
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

    val playerState: StateFlow<Pair<Int?, PlayerState>> = playerRepo.playerState
        .onEach { (idx, state) -> selectedChapterIndx = idx }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null to PlayerState.Idle
        )

    var repeatEnabled by mutableStateOf(false)
        private set

    fun playerAction(action: PlayerAction) {
        viewModelScope.launch(Dispatchers.Default) {
            when (action) {
                PlayerAction.Pause -> playerRepo.pause()
                is PlayerAction.Play -> play()
                is PlayerAction.Next -> next()
                is PlayerAction.Previous -> previous()
                is PlayerAction.Repeat -> {
                    repeatEnabled = action.enable
                    playerRepo.enableRepeat(action.enable)
                }

                is PlayerAction.SeekTo -> playerRepo.seekTo(action.positionMs)
            }
        }
    }

    private suspend fun play() {
        if (selectedChapterIndx > -1) {
            playerRepo.play(selectedChapterIndx)
        }
    }

    private suspend fun previous() {
        if (selectedChapterIndx > 0) {
            selectedChapterIndx--
            playerRepo.previous()
        }
    }

    private suspend fun next() {
        if (selectedChapterIndx < chapters.value.lastIndex) {
            selectedChapterIndx++
            playerRepo.next()
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

    fun downloaderAction(id: String, action: DownloaderAction) {
        viewModelScope.launch(Dispatchers.Default) {
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