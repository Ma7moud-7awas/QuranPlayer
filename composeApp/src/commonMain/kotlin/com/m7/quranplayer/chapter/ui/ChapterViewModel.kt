package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastForEach
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class ChapterViewModel(
    private val chapterRepo: ChapterRepo,
    private val playerRepo: PlayerRepo,
    private val downloaderRepo: DownloaderRepo,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(3)
) : ViewModel() {

    fun onLanguageChanged() {
        viewModelScope.launch(defaultDispatcher) {
            delay(50)
            loadChapters()
        }
    }

    private val originalChapters = MutableStateFlow<List<Chapter>>(emptyList())

    val chapters: StateFlow<List<Chapter>>
        field = MutableStateFlow(emptyList())

    init {
        // initial load
        loadChapters()

        collectPlayerStateUpdates()

        // route media center actions
        collectCenterActionUpdates()

        // observe & update download state
        collectDownloaderStateUpdates()
    }

    private fun loadChapters() {
        viewModelScope.launch(defaultDispatcher) {
            originalChapters.updateAndGet {
                chapterRepo.getChapters(
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
        viewModelScope.launch(defaultDispatcher) {
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
        setSelectedIndex(-1)
        chapters.update { newChapters }
        playerRepo.setPlaylist(newChapters)
    }

    var selectedChapterIndx by mutableIntStateOf(-1)
        private set

    fun setSelectedIndex(index: Int) {
        when (index) {
            -1 -> {
                // deselect state. "reset"
                selectedChapterIndx = index
                playerAction(PlayerAction.Pause)
            }

            selectedChapterIndx if playerState is PlayerState.Playing -> {
                // pause the same chapter.
                playerAction(PlayerAction.Pause)
            }

            else -> {
                // play a new chapter.
                selectedChapterIndx = index
                resetPlayerState()
                playerAction(PlayerAction.Play)
            }
        }
    }

    var playerState by mutableStateOf<PlayerState>(PlayerState.Idle)

    private fun resetPlayerState() {
        playerState = PlayerState.Loading
    }

    private fun collectPlayerStateUpdates() {
        viewModelScope.launch(defaultDispatcher) {
            playerRepo.playerState.collectLatest { (idx, state) ->
                selectedChapterIndx = idx
                playerState = state
            }
        }
    }

    var repeatEnabled by mutableStateOf(false)
        private set

    private fun collectCenterActionUpdates() {
        viewModelScope.launch(defaultDispatcher) {
            playerRepo.playerAction.collectLatest { playerAction(it) }
        }
    }

    fun playerAction(action: PlayerAction) {
        viewModelScope.launch(defaultDispatcher) {
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
            resetPlayerState()

            selectedChapterIndx--
            playerRepo.previous()
        }
    }

    private suspend fun next() {
        if (selectedChapterIndx < chapters.value.lastIndex) {
            resetPlayerState()

            selectedChapterIndx++
            playerRepo.next()
        }
    }

    private fun collectDownloaderStateUpdates() {
        viewModelScope.launch(defaultDispatcher) {
            downloaderRepo.downloadState.collect { (downloadId, state) ->
                Log("downloadId= $downloadId - state= $state")

                originalChapters.update {
                    it.map { chapter ->
                        if (chapter.id == downloadId)
                            chapter.copy(downloadState = state) else chapter
                    }
                }

                chapters.update {
                    // update only the displayed chapters
                    it.map { chapter ->
                        if (chapter.id == downloadId)
                            chapter.copy(downloadState = state) else chapter
                    }
                }

                if (state == DownloadState.NotDownloaded
                    || state is DownloadState.Paused
                    || state is DownloadState.Error
                ) {
                    downloadedAllEnabled = true
                }

                if (state == DownloadState.NotDownloaded
                    || state == DownloadState.Completed
                ) {
                    updateDownloadedCount()
                }
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
        viewModelScope.launch(defaultDispatcher) {
            if (start) {
                downloadedAllEnabled = false
                originalChapters.value.fastForEach { (id, _, _, _, downloadState) ->
                    if (downloadState == DownloadState.NotDownloaded ||
                        downloadState is DownloadState.Paused
                    ) {
                        downloaderRepo.start(id)
                    }
                }
            } else {
                downloadedAllEnabled = true
                originalChapters.value.fastForEach { (id, _, _, _, downloadState) ->
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
        viewModelScope.launch(defaultDispatcher) {
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