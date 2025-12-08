package com.m7.mediaplayer.chapter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.mediaplayer.chapter.data.ChaptersRepoImpl.Companion.chaptersList
import com.m7.mediaplayer.chapter.domain.model.Chapter
import com.m7.mediaplayer.core.ui.component.AnimatedChapterNumber
import com.m7.mediaplayer.core.ui.theme.GreenGrey
import com.m7.mediaplayer.core.ui.theme.LightGreenGrey
import com.m7.mediaplayer.core.ui.theme.MediaPlayerTheme
import com.m7.mediaplayer.core.ui.theme.Orange
import mediaplayer.composeapp.generated.resources.Res
import mediaplayer.composeapp.generated.resources.chapter_number
import mediaplayer.composeapp.generated.resources.chapters
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview(showBackground = true, locale = "ar")
fun ChapterListScreenPreview() {
    MediaPlayerTheme {
        ChapterListScreen(chaptersList.take(12), false, 1, {})
    }
}

@Composable
fun ChapterListScreen(
    chapters: List<Chapter>,
    isPlaying: Boolean,
    selectedChapterIndx: Int,
    onChapterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 16.dp),
    ) {

        // appbar
        item(key = "chapters_appbar") {
            Text(
                stringResource(Res.string.chapters),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = GreenGrey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(LightGreenGrey)
                    .padding(16.dp)
            )
        }

        itemsIndexed(chapters, key = { _, chapter -> chapter.id }) { i, chapter ->
            ChapterItem(
                chapter = chapter,
                isSelected = isPlaying && selectedChapterIndx == i,
                modifier = Modifier.clickable { onChapterSelected(i) }
            )
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isSelected: Boolean,
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
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // number
            AnimatedChapterNumber(
                isSelected = isSelected,
                chapterNum = chapter.trimmedNumber,
                modifier = Modifier.padding(8.dp)
            )

            // title
            Text(
                text = stringResource(
                    chapter.title ?: Res.string.chapter_number, chapter.trimmedNumber
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
            )
        }
    }
}