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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import quranplayer.composeapp.generated.resources.search_by_name

@Composable
@Preview(showBackground = true, locale = "ar")
fun ChaptersToolbarPreview() {
    ChaptersToolbar(2, false, {}, {})
}

@Composable
@Preview(showBackground = true, locale = "ar")
fun ChaptersToolbar_search() {
    ChaptersToolbar(2, true, {}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersToolbar(
    downloadedChaptersCount: Int,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(56.dp).background(LightGreenGrey, containerShape)
        ) {
            // title
            if (searchExpanded) {
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
                    modifier = Modifier.weight(1f)
                )
            } else {
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
                onCheckedChange = {
                    searchText = ""
                    onSearch(searchText)
                    onExpandSearch(it)
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
            modifier = Modifier.fillMaxWidth(.4f)
        )

        // value
        Text(
            "$downloadedChaptersCount / 114",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}