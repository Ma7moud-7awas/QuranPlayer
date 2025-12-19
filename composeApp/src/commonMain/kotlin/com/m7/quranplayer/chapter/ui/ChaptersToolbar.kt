package com.m7.quranplayer.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.core.ui.theme.GreenGrey
import com.m7.quranplayer.core.ui.theme.LightGreenGrey
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.chapters
import quranplayer.composeapp.generated.resources.downloads
import quranplayer.composeapp.generated.resources.language

@Composable
@Preview(showBackground = true)
fun ChaptersToolbarPreview() {
    ChaptersToolbar(2)
}

@Composable
fun ChaptersToolbar(downloadedChaptersCount: Int, modifier: Modifier = Modifier) {
    // todo: add search, download all, language
    val containerShape = RoundedCornerShape(10)
    OutlinedCard(
        shape = containerShape,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(LightGreenGrey, containerShape)
            ) {
                // title
                Text(
                    stringResource(Res.string.chapters),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = GreenGrey,
                    modifier = Modifier.weight(1f)
                )

                // menu toggle
                IconToggleButton(
                    checked = expanded,
                    onCheckedChange = { expanded = it },
                ) {
                    Icon(Icons.Rounded.MoreVert, "Download all")
                }
            }
        }

        // menu
        OptionsStack(expanded) {
            LanguageRow()

            DownloadRow(downloadedChaptersCount)
        }
    }
}

@Composable
fun LanguageRow(modifier: Modifier = Modifier) {
    OptionsRow(
        modifier = modifier
            .padding(5.dp)
            .border(.5.dp, LightGreenGrey, RoundedCornerShape(30))
    ) {
        Icon(
            Icons.Rounded.Language, null,
            modifier = Modifier.padding(12.dp)
        )

        // label
        Text(
            stringResource(Res.string.language) + " :",
            modifier = Modifier.fillMaxWidth(.4f)
        )

        // value
        Text(
            "English",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DownloadRow(downloadedChaptersCount: Int, modifier: Modifier = Modifier) {
    OptionsRow(
        modifier = modifier
            .padding(5.dp)
            .border(.5.dp, LightGreenGrey, RoundedCornerShape(30))
            .padding(end = 15.dp)
    ) {
        Icon(
            Icons.Rounded.Download, null,
            modifier = Modifier.padding(12.dp)
        )

        // label
        Text(
            stringResource(Res.string.downloads) + " :",
            modifier = Modifier.fillMaxWidth(.42f)
        )

        // value
        Text(
            "$downloadedChaptersCount / 114",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        CircularProgressIndicator(
            { .5f },
            modifier = Modifier.size(25.dp)
        )
    }
}