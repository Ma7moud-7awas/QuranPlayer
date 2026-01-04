package com.m7.quranplayer.chapter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.core.ui.theme.Green40
import com.m7.quranplayer.core.ui.theme.GreenGrey
import com.m7.quranplayer.core.ui.theme.LightGreenGrey
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.app_will_be_restarted
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
            searchExpanded = { false },
            onExpandSearch = {},
            onSearch = {},
            changeLanguage = {},
            downloadedChaptersCount = { 12 },
            downloadedAllEnabled = { true },
            downloadAll = {},
        )
    }
}

@Composable
@Preview(showBackground = true, locale = "ar")
fun ChaptersToolbar_SearchExpanded() {
    QuranPlayerTheme {
        ChaptersToolbar(
            searchExpanded = { true },
            onExpandSearch = {},
            onSearch = {},
            changeLanguage = {},
            downloadedChaptersCount = { 72 },
            downloadedAllEnabled = { false },
            downloadAll = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersToolbar(
    searchExpanded: () -> Boolean,
    onExpandSearch: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    changeLanguage: (String) -> Unit,
    downloadedChaptersCount: () -> Int,
    downloadedAllEnabled: () -> Boolean,
    downloadAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(10)
    OutlinedCard(
        shape = containerShape,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val (menuExpanded, expandMenu) = remember { mutableStateOf(false) }

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        var searchText by rememberSaveable { mutableStateOf("") }
        val searchIcon =
            if (searchExpanded()) Icons.AutoMirrored.Rounded.ArrowForward
            else Icons.Rounded.Search

        LaunchedEffect(searchExpanded) {
            // show keyboard
            if (searchExpanded())
                focusRequester.requestFocus()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(56.dp).background(LightGreenGrey, containerShape)
        ) {
            if (searchExpanded()) {
                // search
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors()
                        .copy(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                    placeholder = {
                        Text(
                            stringResource(Res.string.search_by_name),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            onSearch(searchText)
                        }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                )
            } else {
                // title
                Text(
                    stringResource(Res.string.chapters),
                    textAlign = TextAlign.Center,
                    color = GreenGrey,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                        .clickable { onExpandSearch(true) }
                )
            }

            // search toggle
            IconToggleButton(
                checked = searchExpanded(),
                onCheckedChange = { expand ->
                    if (!expand) {
                        // reset search & list
                        searchText = ""
                        onSearch(searchText)
                    }
                    onExpandSearch(expand)
                },
            ) {
                Icon(searchIcon, "Toggle Search")
            }

            // menu toggle
            IconToggleButton(
                checked = menuExpanded,
                onCheckedChange = expandMenu,
            ) {
                Icon(Icons.Rounded.MoreVert, "Download all")
            }
        }

        // menu
        OptionsStack(menuExpanded) {
            val borderModifier = Modifier
                .padding(5.dp)
                .border(.5.dp, LightGreenGrey, RoundedCornerShape(30))

            DownloadRow(
                downloadedChaptersCount = downloadedChaptersCount,
                downloadedAllEnabled = downloadedAllEnabled,
                downloadAll = downloadAll,
                modifier = borderModifier
            )

            LanguageRow(changeLanguage, borderModifier)
        }
    }
}

@Composable
fun DownloadRow(
    downloadedChaptersCount: () -> Int,
    downloadedAllEnabled: () -> Boolean,
    downloadAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OptionsRow(modifier = modifier) {
        Icon(
            Icons.Rounded.Download, null,
            modifier = Modifier.padding(12.dp)
        )

        // label
        Text(
            stringResource(Res.string.downloads) + " :",
            modifier = Modifier.fillMaxWidth(.4f)
        )

        // value
        Text(
            stringResource(
                Res.string.downloads_count,
                downloadedChaptersCount().localize()
            ),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // download all btn & progress
        if (downloadedChaptersCount() < 114) {
            if (downloadedAllEnabled()) {
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
                        progress = { downloadedChaptersCount() / 114f },
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

@Composable
fun LanguageRow(
    changeLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (menuExpanded, expandMenu) = remember { mutableStateOf(false) }
    val languages = mapOf(
        "ar" to "العربية",
        "en" to "English",
    )

    OptionsRow(modifier = modifier) {
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

        // expand btn
        IconButton(onClick = { expandMenu(!menuExpanded) }) {
            Icon(
                Icons.Rounded.KeyboardArrowDown, null,
                modifier = Modifier.let {
                    if (menuExpanded) it.rotate(180f) else it
                }
            )
        }
    }

    AnimatedVisibility(menuExpanded) {
        Column {
            // hint
            Text(
                stringResource(Res.string.app_will_be_restarted),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Orange,
                modifier = Modifier.fillMaxWidth()
            )

            // language options
            languages
                .forEach { lang ->
                    key(lang.key) {
                        val selected by remember {
                            derivedStateOf { lang.key == Locale.current.language }
                        }

                        val color = if (selected) LightGreenGrey else Green40

                        Text(
                            lang.value,
                            textAlign = TextAlign.Center,
                            color = color,
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 60.dp)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .border(1.dp, color, RoundedCornerShape(30))
                                .padding(5.dp)
                                .let {
                                    if (!selected)
                                        it.clickable {
                                            expandMenu(false)
                                            changeLanguage(lang.key)
                                        }
                                    else it
                                }
                        )
                    }
                }
        }
    }
}