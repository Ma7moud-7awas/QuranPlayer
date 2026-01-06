package com.m7.quranplayer.chapter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.domain.model.Part
import com.m7.quranplayer.chapter.ui.toolbar.ChaptersToolbar
import com.m7.quranplayer.core.di.format
import com.m7.quranplayer.core.ui.theme.LightGray
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.model.DownloaderAction
import com.m7.quranplayer.downloader.ui.DownloadStack
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.ui.PlayerStack
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.allStringResources
import quranplayer.composeapp.generated.resources.chapters

val chaptersFakeList: List<Chapter>
    @Composable
    get() = buildList {
        for (i in 1..9) {
            val fId = i.format("%03d")
            add(
                Chapter(
                    id = fId,
                    number = "$i",
                    title = stringResource(Res.allStringResources[fId] ?: return@buildList),
                    parts = Part.getPartsByChapterNumber(i)
                )
            )
        }
    }


//@Preview(showBackground = true, locale = "ar")
@Composable
private fun ChapterListScreenPreview() {
    QuranPlayerTheme {
        ChapterListScreen(
            rememberLazyListState(),
            PaddingValues(0.dp),
            {},
            {},
            {}
        )
    }
}

@Composable
fun ChapterListScreen(
    listState: LazyListState,
    innerPadding: PaddingValues,
    changeLanguage: (String) -> Unit,
    onStateChanged: (PlayerState) -> Unit,
    onSelectedItemChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapterViewModel: ChapterViewModel = koinViewModel()

    val chapters by chapterViewModel.chapters.collectAsStateWithLifecycle()
    val playerState by chapterViewModel.playerState.collectAsStateWithLifecycle()

    LaunchedEffect(playerState) { onStateChanged(playerState) }

    LaunchedEffect(chapterViewModel.selectedChapterIndx) {
        onSelectedItemChanged(chapterViewModel.selectedChapterIndx)
    }

    val (searchExpanded, setSearchExpanded) = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = 120.dp
        ),
        state = listState
    ) {
        item(key = Res.string.chapters.key) {
            ChaptersToolbar(
                searchExpanded = { searchExpanded },
                onExpandSearch = setSearchExpanded,
                onSearch = chapterViewModel::search,
                changeLanguage = {
                    changeLanguage(it)
                    chapterViewModel.onLanguageChanged()

                },
                downloadedChaptersCount = { chapterViewModel.downloadedChaptersCount },
                downloadedAllEnabled = { chapterViewModel.downloadedAllEnabled },
                downloadAll = chapterViewModel::downloadAll,
            )

            Filter(
                expand = { searchExpanded },
                selectedPart = { chapterViewModel.selectedPart },
                onPartSelected = chapterViewModel::onPartSelected
            )
        }

        itemsIndexed(chapters, key = { _, chapter -> chapter.id }) { i, chapter ->
            ChapterItem(
                chapter = { chapter },
                isSelected = { chapterViewModel.selectedChapterIndx == i },
                playerState = { playerState },
                playerAction = chapterViewModel::playerAction,
                downloaderAction = chapterViewModel::downloaderAction,
                isRepeatEnabled = { chapterViewModel.repeatEnabled },
                onCardClicked = { chapterViewModel.setSelectedChapterIndex(i) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PartItem() {
    Row {
        PartItem({ Part.Fifth }, { true })
        PartItem({ Part.TwentyFifth }, { false })
    }
}

@Composable
fun PartItem(part: () -> Part, isSelected: () -> Boolean, modifier: Modifier = Modifier) {
    val color = if (isSelected()) Orange else MaterialTheme.colorScheme.surfaceVariant

    OutlinedCard(
        modifier = modifier.padding(horizontal = 5.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = (part().ordinal + 1).toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(35.dp)
                .padding(vertical = 5.dp)
        )
    }
}

@Preview(showBackground = true, locale = "")
@Composable
private fun ChapterItemPreview() {
    val chapter = chaptersFakeList.first().copy(downloadState = DownloadState.NotDownloaded)
    QuranPlayerTheme {
        ChapterItem(
            chapter = { chapter },
            isSelected = { true },
            playerState = { PlayerState.Idle },
            playerAction = {},
            downloaderAction = { _, _ -> },
            isRepeatEnabled = { false },
            onCardClicked = {}
        )
    }
}

@Composable
fun ChapterItem(
    chapter: () -> Chapter,
    isSelected: () -> Boolean,
    playerState: () -> PlayerState,
    playerAction: (PlayerAction) -> Unit,
    downloaderAction: (String, DownloaderAction) -> Unit,
    isRepeatEnabled: () -> Boolean,
    onCardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by rememberUpdatedState { isSelected() && playerState() is PlayerState.Playing }
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
            repeatEnabled = isRepeatEnabled,
        )

        ChapterCard(
            chapter = chapter,
            isPlaying = isPlaying,
            onDownloadClicked = { expanded = !expanded },
            modifier = Modifier.clickable(onClick = onCardClicked)
        )

        DownloadStack(
            chapter = chapter,
            expanded = { expanded },
            downloaderAction = downloaderAction
        )
    }
}

@Preview(locale = "ar")
@Composable
private fun ChapterCard() {
    QuranPlayerTheme {
        val chapter = chaptersFakeList[2]
        ChapterCard(
            { chapter },
            { false },
            { },
        )
    }
}

@Composable
fun ChapterCard(
    chapter: () -> Chapter,
    isPlaying: () -> Boolean,
    onDownloadClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isPlaying()) Orange.copy(.75f)
        else CardDefaults.cardColors().containerColor
    )

    val downloadIcon =
        if (chapter().downloadState is DownloadState.Completed) Icons.Rounded.DownloadDone
        else Icons.Rounded.Download

    Card(
        colors = CardDefaults.cardColors().copy(containerColor = cardBgColor),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // number
            AnimatedChapterNumber(
                isPlaying = isPlaying,
                chapterNum = { chapter().number },
                modifier = Modifier.padding(8.dp)
            )

            // title
            Text(
                text = chapter().title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
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