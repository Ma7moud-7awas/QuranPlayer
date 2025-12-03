package com.m7.mediaplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m7.mediaplayer.ui.ChapterPlayer
import com.m7.mediaplayer.ui.theme.MediaPlayerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun App() {
    MediaPlayerTheme {
        Box {
            var expandPlayer by remember { mutableStateOf(false) }

            ChapterListScreen(
                isPaused = !expandPlayer,
                onChapterSelected = {
                    expandPlayer = true
                }
            )

            ChapterPlayer(
                expand = expandPlayer,
                onExpand = {
                    expandPlayer = !expandPlayer
                },
                progress = .5f,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

