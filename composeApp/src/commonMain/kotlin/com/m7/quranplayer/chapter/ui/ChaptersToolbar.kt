package com.m7.quranplayer.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.core.ui.theme.GreenGrey
import com.m7.quranplayer.core.ui.theme.LightGreenGrey
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.chapters
import quranplayer.composeapp.generated.resources.current_language
import quranplayer.composeapp.generated.resources.download_all
import quranplayer.composeapp.generated.resources.downloads
import quranplayer.composeapp.generated.resources.downloads_count
import quranplayer.composeapp.generated.resources.language
import quranplayer.composeapp.generated.resources.search_by_name

@Composable
@Preview(showBackground = true)
fun ChaptersToolbarPreview_SearchCollapsed() {
    QuranPlayerTheme {
        ChaptersToolbar(
            downloadedChaptersCount = 2,
            downloadedAllEnabled = false,
            downloadAll = {},
            searchExpanded = false,
            onExpandSearch = {},
            onSearch = {}
        )
    }
}

@Composable
@Preview(showBackground = true, locale = "ar")
fun ChaptersToolbar_SearchExpanded() {
    QuranPlayerTheme {
        ChaptersToolbar(
            downloadedChaptersCount = 2,
            downloadedAllEnabled = false,
            downloadAll = {},
            searchExpanded = true,
            onExpandSearch = {},
            onSearch = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersToolbar(
    downloadedChaptersCount: Int,
    downloadedAllEnabled: Boolean,
    downloadAll: (Boolean) -> Unit,
    searchExpanded: Boolean,
    onExpandSearch: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(10)
    OutlinedCard(
        shape = containerShape,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        var expandMenu by rememberSaveable { mutableStateOf(false) }

        var searchText by rememberSaveable { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(searchExpanded) {
            // show keyboard
            if (searchExpanded)
                focusRequester.requestFocus()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(56.dp).background(LightGreenGrey, containerShape)
        ) {
            if (searchExpanded) {
                // search
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors()
                        .copy(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                    placeholder = {
                        Text(
                            stringResource(Res.string.search_by_name),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(searchText) }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                )
            } else {
                // title
                Text(
                    stringResource(Res.string.chapters),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = GreenGrey,
                    modifier = Modifier.weight(1f)
                        .clickable { onExpandSearch(true) }
                )
            }

            // search toggle
            IconToggleButton(
                checked = searchExpanded,
                onCheckedChange = { expand ->
                    if (!expand) {
                        // reset search & list
                        searchText = ""
                        onSearch(searchText)
                    }
                    onExpandSearch(expand)
                },
            ) {
                Icon(Icons.Rounded.Search, "Search")
            }

            // menu toggle
            IconToggleButton(
                checked = expandMenu,
                onCheckedChange = { expandMenu = it },
            ) {
                Icon(Icons.Rounded.MoreVert, "Download all")
            }
        }

        // menu
        OptionsStack(expandMenu) {
            LanguageRow()

            DownloadRow(downloadedChaptersCount, downloadedAllEnabled, downloadAll)
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
            stringResource(Res.string.current_language),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DownloadRow(
    downloadedChaptersCount: Int,
    downloadedAllEnabled: Boolean,
    downloadAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
            stringResource(
                Res.string.downloads_count,
                downloadedChaptersCount.localize()
            ),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // download all btn & progress
        if (downloadedChaptersCount < 114) {
            if (downloadedAllEnabled) {
                TextButton(onClick = { downloadAll(true) }) {
                    Text(
                        text = stringResource(Res.string.download_all),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            } else {
                IconButton({ downloadAll(false) }) {
                    CircularProgressIndicator(
                        progress = { downloadedChaptersCount / 114f },
                        modifier = Modifier.padding(5.dp)
                    )

                    Icon(
                        Icons.Rounded.Stop,
                        "Stop downloading",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}