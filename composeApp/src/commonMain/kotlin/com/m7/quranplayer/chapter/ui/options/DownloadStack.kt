package com.m7.quranplayer.chapter.ui.options

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview(showBackground = true)
fun DownloadStack(expanded: Boolean = true, modifier: Modifier = Modifier) {
    OptionsStack(expanded, modifier) {
        OptionsRow(
            options = {
                IconButton({}) {
                    Icon(Icons.Rounded.Stop, "Stop Download")
                }

                IconButton({}) {
                    Icon(Icons.Rounded.Pause, "Pause/Play Download")
                }

                CircularProgressIndicator(
                    progress = { .6f },
                    modifier = Modifier.size(25.dp)
                )
            }
        )
    }
}