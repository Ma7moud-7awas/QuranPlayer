package com.m7.mediaplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m7.mediaplayer.ui.ChapterPlayer
import com.m7.mediaplayer.ui.theme.MediaPlayerTheme
import mediaplayer.composeapp.generated.resources.Res
import mediaplayer.composeapp.generated.resources.bg_light
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    MediaPlayerTheme {
        Box(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val chapterViewModel: ChapterViewModel = koinViewModel()

            val chapters by chapterViewModel.chapters.collectAsStateWithLifecycle()
            val isLoading by chapterViewModel.loading.collectAsStateWithLifecycle()

            var isPlayerExpanded by remember { mutableStateOf(false) }
            var selectedChapterId by remember { mutableIntStateOf(0) }

            // background
            Image(
                painterResource(Res.drawable.bg_light),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )

            if (isLoading) {
                CircularWavyProgressIndicator(
                    progress = { .9f },
                    wavelength = 25.dp,
                    waveSpeed = 35.dp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ChapterListScreen(
                    chapters,
                    isPlaying = isPlayerExpanded,
                    selectedChapterId = selectedChapterId,
                    onChapterSelected = {
                        Log("onChapterSelected")
                        selectedChapterId = it
                        chapterViewModel.play(chapters[it-1])
                        isPlayerExpanded = true
                    }
                )

                ChapterPlayer(
                    chapter = chapters.getOrNull(selectedChapterId - 1),
                    expanded = isPlayerExpanded,
                    onPlay = {
                        Log("onPlay")
                        if (selectedChapterId == 0) {
                            // todo: play last cashed chapter
                            selectedChapterId++
                        }
                        isPlayerExpanded = true
                    },
                    onPause = {
                        Log("onPause")
                        isPlayerExpanded = false
                    },
                    onPrevious = {
                        Log("onPrevious")
                        selectedChapterId--
                    },
                    onNext = {
                        Log("onNext")
                        if (selectedChapterId < chapters.size) {
                            selectedChapterId++
                        } else {
                            isPlayerExpanded = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}