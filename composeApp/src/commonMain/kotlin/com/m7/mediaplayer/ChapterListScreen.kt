package com.m7.mediaplayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.m7.mediaplayer.model.Chapter
import com.m7.mediaplayer.ui.component.AnimatedChapterNumber
import com.m7.mediaplayer.ui.theme.GreenGrey
import com.m7.mediaplayer.ui.theme.LightGreenGrey
import com.m7.mediaplayer.ui.theme.MediaPlayerTheme
import com.m7.mediaplayer.ui.theme.Orange
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChapterListScreenPreview() {
    MediaPlayerTheme {
        ChapterListScreen(chaptersPlaceholder, false, 1, {})
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    chapters: List<Chapter>,
    isPlaying: Boolean,
    selectedChapterId: Int,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // appbar
        Text(
            "Chapters",
            textAlign = TextAlign.Center,
            color = GreenGrey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(CircleShape)
                .background(LightGreenGrey)
                .padding(16.dp)
        )

        // list
        Chapters(
            chapters,
            isPlaying = isPlaying,
            selectedChapterId = selectedChapterId,
            onChapterSelected = { onChapterSelected(it) }
        )
    }
}

@Composable
fun Chapters(
    chapters: List<Chapter>,
    isPlaying: Boolean,
    selectedChapterId: Int,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize()) {
        items(chapters, key = { it.id }) { chapter ->
            ChapterItem(
                chapter = chapter,
                isSelected = selectedChapterId == chapter.id && isPlaying,
                onChapterSelected = {
                    onChapterSelected(it)
                }
            )
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isSelected) Orange.copy(.75f) else CardDefaults.cardColors().containerColor
    )

    Card(
        colors = CardDefaults.cardColors().copy(containerColor = cardBgColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clickable { onChapterSelected(chapter.id) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedChapterNumber(
                isSelected = isSelected,
                chapterNum = chapter.id,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = chapter.title,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
            )
        }
    }
}