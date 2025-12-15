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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m7.quranplayer.chapter.data.ChaptersRepoImpl.Companion.chaptersList
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.ui.options.DownloadStack
import com.m7.quranplayer.chapter.ui.options.PlayerStack
import com.m7.quranplayer.core.ui.theme.GreenGrey
import com.m7.quranplayer.core.ui.theme.LightGray
import com.m7.quranplayer.core.ui.theme.LightGreenGrey
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.chapters

@Composable
//@Preview(showBackground = true, locale = "ar")
fun ChapterListScreenPreview() {
    QuranPlayerTheme {
//        ChapterListScreen()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onSelectedItemChanged: (Int) -> Unit
) {
    val chapterViewModel: ChapterViewModel = koinViewModel()

    val chapters by chapterViewModel.chapters.collectAsStateWithLifecycle()
    val playerState by chapterViewModel.playerState.collectAsStateWithLifecycle()

    LaunchedEffect(chapterViewModel.selectedChapterIndx) {
        onSelectedItemChanged(chapterViewModel.selectedChapterIndx)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        state = listState
    ) {

        item(key = Res.string.chapters.key) {
            // todo: add search, download all, language
            Text(
                stringResource(Res.string.chapters),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = GreenGrey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightGreenGrey)
                    .padding(16.dp)
            )
        }

        itemsIndexed(chapters, key = { _, chapter -> chapter.id }) { i, chapter ->
            ChapterItem(
                chapter = chapter,
                isSelected = chapterViewModel.selectedChapterIndx == i,
                playerState = playerState,
                playerAction = chapterViewModel::playerAction,
                modifier = Modifier.clickable {
                    chapterViewModel.setSelectedIndex(i)
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true, locale = "")
fun ChapterItemPreview() {
    QuranPlayerTheme {
        ChapterItem(chaptersList.first(), true, PlayerState.Idle, {})
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    playerState: PlayerState,
    playerAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by rememberUpdatedState { isSelected && playerState is PlayerState.Playing }
    var expanded by remember { mutableStateOf(true) }

    val cardBgColor by animateColorAsState(
        targetValue = if (isPlaying()) Orange.copy(.75f)
        else CardDefaults.cardColors().containerColor
    )

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {

        PlayerStack(isSelected, isPlaying, playerState, playerAction)

        Card(
            colors = CardDefaults.cardColors().copy(containerColor = cardBgColor),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // number
                AnimatedChapterNumber(
                    isPlaying = isPlaying(),
                    chapterNum = chapter.number,
                    modifier = Modifier.padding(8.dp)
                )

                // title
                Text(
                    text = stringResource(chapter.titleRes, chapter.number),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp).weight(1f)
                )

                Spacer(Modifier.height(30.dp).width(.5.dp).background(LightGray))

                // download menu toggle
                IconButton(
                    { expanded = !expanded },
                    colors = IconButtonDefaults.iconButtonColors().copy(contentColor = LightGray),
                ) {
                    Icon(Icons.Rounded.Download, null)
                }
            }
        }

        DownloadStack(expanded)
    }
}