package com.m7.quranplayer.chapter.ui.toolbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.di.localize
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.theme.digitsTextStyle
import org.jetbrains.compose.resources.stringResource
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.download_all
import quranplayer.composeapp.generated.resources.downloads
import quranplayer.composeapp.generated.resources.downloads_count

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
            style = digitsTextStyle,
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