package com.m7.quranplayer.chapter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.core.ui.theme.LightGray
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.model.DownloaderAction
import com.m7.quranplayer.downloader.ui.DownloadStack
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.ui.PlayerStack
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.chapters

val chaptersFakeList = buildList {
    for (i in 1..10)
        add(Chapter("$i", "$i", "title $i"))
}

@Composable
//@Preview(showBackground = true, locale = "ar")
private fun ChapterListScreenPreview() {
    QuranPlayerTheme {
//        ChapterListScreen()
    }
}

@Composable
fun ChapterListScreen(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onSelectedItemChanged: (Int) -> Unit
) {
    val chapterViewModel: ChapterViewModel = koinViewModel()

    val chapters by chapterViewModel.chapters.collectAsStateWithLifecycle()
    val playerState by chapterViewModel.playerState.collectAsStateWithLifecycle()

    var searchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(chapterViewModel.selectedChapterIndx) {
        onSelectedItemChanged(chapterViewModel.selectedChapterIndx)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        state = listState
    ) {
        item(key = Res.string.chapters.key) {
            ChaptersToolbar(
                chapterViewModel.downloadedChaptersCount,
                searchExpanded,
                onExpandSearch = { searchExpanded = it },
                onSearch = chapterViewModel::search
            )
        }

        itemsIndexed(chapters, key = { _, chapter -> chapter.id }) { i, chapter ->
            ChapterItem(
                chapter = chapter,
                isSelected = chapterViewModel.selectedChapterIndx == i,
                playerState = playerState,
                playerAction = chapterViewModel::playerAction,
                downloaderAction = chapterViewModel::downloaderAction,
                isRepeatEnabled = chapterViewModel.isRepeatEnabled,
                onRepeatClicked = chapterViewModel::onRepeatClicked,
                onCardClicked = { chapterViewModel.setSelectedIndex(i) }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "ar")
private fun ChapterItemPreview() {
    QuranPlayerTheme {
        ChapterItem(
            chapter = chaptersFakeList.first().copy(downloadState = DownloadState.NotDownloaded),
            isSelected = true,
            playerState = PlayerState.Idle,
            playerAction = {},
            downloaderAction = { _, _ -> },
            isRepeatEnabled = false,
            onRepeatClicked = {},
            onCardClicked = {}
        )
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    playerState: PlayerState,
    playerAction: (PlayerAction) -> Unit,
    downloaderAction: (String, DownloaderAction) -> Unit,
    isRepeatEnabled: Boolean,
    onRepeatClicked: (Boolean) -> Unit,
    onCardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by rememberUpdatedState { isSelected && playerState is PlayerState.Playing }
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        PlayerStack(
            isSelected = isSelected,
            isPlaying = isPlaying,
            playerState = playerState,
            playerAction = playerAction,
            isRepeatEnabled = isRepeatEnabled,
            onRepeatClicked = onRepeatClicked
        )

        ChapterCard(
            chapter = chapter,
            isPlaying = isPlaying(),
            onDownloadClicked = { expanded = !expanded },
            modifier = Modifier.clickable(onClick = onCardClicked)
        )

        DownloadStack(
            chapter = chapter,
            expanded = expanded,
            downloaderAction = downloaderAction
        )
    }
}

@Composable
fun ChapterCard(
    chapter: Chapter,
    isPlaying: Boolean,
    onDownloadClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isPlaying) Orange.copy(.75f)
        else CardDefaults.cardColors().containerColor
    )

    val downloadIcon =
        if (chapter.downloadState is DownloadState.Completed) Icons.Rounded.DownloadDone
        else Icons.Rounded.Download

    Card(
        colors = CardDefaults.cardColors().copy(containerColor = cardBgColor),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // number
            AnimatedChapterNumber(
                isPlaying = isPlaying,
                chapterNum = chapter.number,
                modifier = Modifier.padding(8.dp)
            )

            // title
            Text(
                text = chapter.title,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp).weight(1f)
            )

            // v separator
            Spacer(Modifier.height(30.dp).width(.5.dp).background(LightGray))

            // download menu toggle
            IconButton(
                onClick = onDownloadClicked,
                colors = IconButtonDefaults.iconButtonColors().copy(contentColor = LightGray),
            ) {
                Icon(downloadIcon, null)
            }
        }
    }
}