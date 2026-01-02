package com.m7.quranplayer.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.chapter.ui.ChapterListScreen
import com.m7.quranplayer.core.ui.theme.Green
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.bg_light
import quranplayer.composeapp.generated.resources.check_device_connection

@Composable
fun App(
    onLanguageChanged: (String) -> Unit
) {
    QuranPlayerTheme {
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        var selectedIndex by remember { mutableIntStateOf(-1) }
        val isPlayingItemInvisible by remember {
            derivedStateOf {
                selectedIndex > -1 && !isItemVisible(selectedIndex, listState)
            }
        }

        var error: String? by remember { mutableStateOf(null) }

        Scaffold(
            floatingActionButton = {
                // error/scroll indicator
                if (error != null) {
                    ErrorButton()
                } else if (isPlayingItemInvisible) {
                    ScrollButton {
                        coroutineScope.launch {
                            listState.animateScrollToItem(selectedIndex + 1)
                        }
                    }
                }
            }
        ) { innerPadding ->
            // background
            Image(
                painterResource(Res.drawable.bg_light),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )

            // content
            ChapterListScreen(
                listState,
                innerPadding = innerPadding,
                changeLanguage = onLanguageChanged,
                onStateChanged = {
                    error = if (it is PlayerState.Error)
                        it.error?.message else null
                },
                onSelectedItemChanged = { selectedIndex = it },
            )

            // status bar background
            Spacer(
                Modifier.fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            .4f to Color.White,
                            1f to Color.Transparent,
                        )
                    )
            )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
fun ErrorButton() {
    val (expanded, setExpanded) = remember { mutableStateOf(true) }

    HorizontalFloatingToolbar(
        expanded = expanded,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors()
            .copy(toolbarContainerColor = Orange),
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                containerColor = Color.Red,
                contentColor = Color.White,
                onClick = { setExpanded(!expanded) },
            ) {
                Icon(Icons.Rounded.Error, null)
            }
        }
    ) {
        Text(
            stringResource(Res.string.check_device_connection),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            modifier = with(LocalDensity.current) {
                val contentWidth =
                    LocalWindowInfo.current.containerSize.width.toDp() - FloatingToolbarDefaults.ContainerSize * 1.7f
                Modifier
                    .width(contentWidth)
                    .padding(horizontal = 5.dp)
            }
        )
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