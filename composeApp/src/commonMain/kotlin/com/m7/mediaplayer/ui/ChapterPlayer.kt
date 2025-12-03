package com.m7.mediaplayer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.mediaplayer.ui.theme.Green
import com.m7.mediaplayer.ui.theme.Orange
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterPlayer(
    expand: Boolean,
    onExpand: (Boolean) -> Unit,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val totalDuration = 500.toDuration(DurationUnit.SECONDS)
    val passedDuration = 250.toDuration(DurationUnit.SECONDS)

    val fabBgColor by animateColorAsState(targetValue = if (expand) Orange else Green)

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = expand,
        floatingActionButton = {
            // play/pause btn
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                containerColor = fabBgColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    onExpand(!expand)
                }
            ) {
                Icon(
                    if (expand) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    "Play"
                )
            }
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // progress bar
            LinearWavyProgressIndicator(
                { progress },
                amplitude = { .5f },
                waveSpeed = 10.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // passed duration
                DurationText(passedDuration.toString())

                // prev btn | todo: check if there is a previous chapter to disable this btn
                IconButton(onClick = { }, Modifier) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }

                // next btn | todo: check if there is a next chapter to disable this btn
                IconButton(onClick = { }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }

                // total duration
                DurationText(totalDuration.toString())
            }
        }
    }
}

@Composable
fun DurationText(duration: String) {
    Text(
        duration,
        fontFamily = FontFamily.Cursive,
        fontSize = 12.sp,
        modifier = Modifier.padding(8.dp)
    )
}