package com.m7.mediaplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.m7.mediaplayer.model.Chapter
import com.m7.mediaplayer.ui.component.AnimatedChapterNumber
import com.m7.mediaplayer.ui.theme.MediaPlayerTheme
import mediaplayer.composeapp.generated.resources.Res
import mediaplayer.composeapp.generated.resources.bg_light
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ChapterListScreenPreview(modifier: Modifier = Modifier) {
    MediaPlayerTheme {
        ChapterListScreen(false, {})
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterListScreen(isPaused: Boolean, onChapterSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .safeContentPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // background
        Image(
            painterResource(Res.drawable.bg_light),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )

        val loading = remember { false }

        if (loading) {
            CircularWavyProgressIndicator(wavelength = 25.dp)
        } else {
            // list
            Chapters(
                listOf(
                    Chapter(1, "Al Fatiha"),
                    Chapter(2, "Al Bakara"),
                    Chapter(3, "A'l Emran"),
                ),
                isPaused = isPaused,
                onSelect = { onChapterSelected(it) }
            )
        }
    }
}

@Composable
fun Chapters(chapters: List<Chapter>, isPaused: Boolean, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var selectedChapterId by remember { mutableIntStateOf(0) }

    LazyColumn(modifier.fillMaxSize()) {
        items(chapters, key = { it.id }) { chapter ->
            ChapterItem(
                chapter = chapter,
                isSelected = selectedChapterId == chapter.id,
                isPaused = isPaused,
                onSelect = {
                    selectedChapterId = it
                    onSelect(it)
                }
            )
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
    isPaused: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSelect(chapter.id) }
    ) {
        Row {
            AnimatedChapterNumber(isSelected && !isPaused, chapter.id)

            Text(
                text = chapter.title,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}