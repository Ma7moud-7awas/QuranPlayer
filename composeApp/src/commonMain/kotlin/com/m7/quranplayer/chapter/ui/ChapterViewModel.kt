package com.m7.quranplayer.chapter.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.model.Part
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
        viewModelScope.launch(Dispatchers.Default) {
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

    var selectedPart by mutableStateOf<Part?>(null)
        private set

    fun onPartSelected(part: Part) {
        viewModelScope.launch(Dispatchers.Default) {
            selectedPart = part
            updateChapters(originalChapters.value.filter { it.parts.contains(part) })
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

            selectedChapterIndx if playerState.value is PlayerState.Playing -> {
                // pause the same chapter.
                playerAction(PlayerAction.Pause)
            }

            else -> {
                // play a new chapter.
                selectedChapterIndx = index
                playerAction(PlayerAction.Play)
                resetPlayerState()
            }
        }
    }

    val playerState: StateFlow<PlayerState>
        field = MutableStateFlow<PlayerState>(PlayerState.Idle)

    private fun resetPlayerState() {
        playerState.update { PlayerState.Loading }
    }

    private fun collectPlayerStateUpdates() {
        viewModelScope.launch(Dispatchers.Default) {
            playerRepo.playerState.collectLatest { (idx, state) ->
                selectedChapterIndx = idx
                playerState.update { state }
            }
        }
    }

    var repeatEnabled by mutableStateOf(false)
        private set

    private fun collectCenterActionUpdates() {
        viewModelScope.launch(Dispatchers.Default) {
            playerRepo.playerAction.collectLatest { playerAction(it) }
        }
    }

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
        viewModelScope.launch(Dispatchers.Default) {
            downloaderRepo.downloadState.collect { (downloadId, state) ->
                Log("downloadId= $downloadId - state= $state")

                if (state == DownloadState.NotDownloaded
                    || state is DownloadState.Paused
                    || state is DownloadState.Error
                ) {
                    downloadedAllEnabled = true
                }

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

                updateDownloadedCount()
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
                originalChapters.value.forEach { (id, _, _, _, downloadState) ->
                    if (downloadState == DownloadState.NotDownloaded
                        || downloadState is DownloadState.Paused
                    ) {
                        downloaderRepo.start(id)
                    }
                }
            } else {
                downloadedAllEnabled = true
                originalChapters.value.forEach { (id, _, _, _, downloadState) ->
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