package com.m7.quranplayer.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m7.quranplayer.chapter.ui.ChapterListScreen
import com.m7.quranplayer.chapter.ui.ChapterViewModel
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.ui.Player
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.bg_light

@Composable
@Preview
fun App(onStateChange: (PlayerState) -> Unit = {}) {
    QuranPlayerTheme {
        Box(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val chapterViewModel: ChapterViewModel = koinViewModel()

            val chapters by chapterViewModel.chapters.collectAsStateWithLifecycle()
            val playerState by chapterViewModel.playerState.collectAsStateWithLifecycle()

            // background
            Image(
                painterResource(Res.drawable.bg_light),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )

            // list
            ChapterListScreen(
                chapters,
                isPlaying = playerState is PlayerState.Playing,
                selectedChapterIndx = chapterViewModel.selectedChapterIndx,
                onChapterSelected = chapterViewModel::setSelectedIndex
            )

            // player
            Player(
                playerState = playerState,
                playerAction = chapterViewModel::playerAction,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )

            // update platform media center with player state
            LaunchedEffect(playerState) {
                onStateChange(playerState)
            }
        }
    }
}