package com.m7.quranplayer.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.m7.quranplayer.chapter.ui.ChapterListScreen
import com.m7.quranplayer.core.ui.theme.Green
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.bg_light

@Composable
fun App(onStateChange: @Composable (PlayerState) -> Unit = {}) {
    QuranPlayerTheme {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var selectedIndex by remember { mutableIntStateOf(-1) }
        val isPlayingItemInvisible by remember {
            derivedStateOf {
                selectedIndex > -1 && !isItemVisible(selectedIndex, listState)
            }
        }

        Scaffold(floatingActionButton = {
            // scroll to the playing item
            if (isPlayingItemInvisible)
                ScrollButton {
                    coroutineScope.launch {
                        listState.animateScrollToItem(selectedIndex + 1)
                    }
                }
        }) { innerPadding ->
            // background
            Image(
                painterResource(Res.drawable.bg_light),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )

            // list
            ChapterListScreen(
                listState,
                onSelectedItemChanged = {
                    selectedIndex = it
                },
                modifier = Modifier.padding(innerPadding)
            )

            // update platform media center with player state
//            LaunchedEffect(playerState) {
//                onStateChange(playerState)
//            }
        }
    }
}

fun isItemVisible(itemIndex: Int, listState: LazyListState): Boolean {
    return listState.layoutInfo.let {
        val padding = 50

        it.visibleItemsInfo
            .filter { itemInfo ->
                itemInfo.offset + padding >= it.viewportStartOffset &&
                        itemInfo.offset - padding + itemInfo.size <= it.viewportEndOffset
            }
            .map { it.index - 1 }
            .any { it == itemIndex }
    }
}

@Composable
@Preview
fun ScrollButton(onClick: () -> Unit = {}) {
    FloatingActionButton(
        containerColor = Orange,
        contentColor = Green,
        onClick = onClick,
    ) {
        Icon(Icons.Rounded.PlayArrow, null)
    }
}