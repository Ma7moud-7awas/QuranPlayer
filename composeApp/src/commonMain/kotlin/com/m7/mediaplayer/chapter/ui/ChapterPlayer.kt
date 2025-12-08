package com.m7.mediaplayer.chapter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.mediaplayer.chapter.domain.model.PlayerAction
import com.m7.mediaplayer.chapter.domain.model.PlayerState
import com.m7.mediaplayer.core.Log
import com.m7.mediaplayer.core.ui.theme.Green
import com.m7.mediaplayer.core.ui.theme.GreenGrey
import com.m7.mediaplayer.core.ui.theme.MediaPlayerTheme
import com.m7.mediaplayer.core.ui.theme.Orange
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
@Preview(locale = "en")
fun ChapterPlayerPreview() {
    MediaPlayerTheme {
        ChapterPlayer(
//            PlayerState.Ended,
//        PlayerState.Paused,
            PlayerState.Playing(10, flowOf(5)),
//        PlayerState.Error(Exception()),
//        PlayerState.Loading,
            playerAction = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterPlayer(
    playerState: PlayerState,
    playerAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var totalMillis by remember { mutableLongStateOf(0) }
    val totalDuration = (totalMillis / 1000).toDuration(DurationUnit.SECONDS)

    var progressMillis by remember { mutableLongStateOf(0) }
    val progressDuration by remember {
        derivedStateOf {
            (progressMillis / 1000).toDuration(DurationUnit.SECONDS)
        }
    }

    val fabBgColor by animateColorAsState(
        targetValue =
            when (playerState) {
                is PlayerState.Playing,
                is PlayerState.Loading -> Orange

                is PlayerState.Error -> Color.Red
                else -> Green
            }
    )

    // update progress ui state when player state changes
    LaunchedEffect(playerState) {
        Log("playerState effect = $playerState")
        when (playerState) {
            is PlayerState.Playing -> {
                totalMillis = playerState.duration
                playerState.updatedPosition.collectLatest { progressMillis = it }
            }

            PlayerState.Ended -> playerAction(PlayerAction.Next)
            else -> Unit
        }
    }

    HorizontalFloatingToolbar(
        modifier = modifier,
        shape = RoundedCornerShape(25),
        expandedShadowElevation = 20.dp,
        contentPadding = PaddingValues(horizontal = 5.dp),
        expanded = playerState.let {
            it is PlayerState.Playing || it is PlayerState.Paused || it is PlayerState.Loading
        },
        floatingActionButton = {
            // play/pause btn
            FloatingToolbarDefaults.StandardFloatingActionButton(
                containerColor = fabBgColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    when (playerState) {
                        PlayerState.Loading -> Unit
                        is PlayerState.Playing -> playerAction(PlayerAction.Pause)
                        else -> playerAction(PlayerAction.Play)
                    }
                }
            ) {
                Icon(
                    when (playerState) {
                        PlayerState.Loading -> Icons.Rounded.Downloading
                        is PlayerState.Error -> Icons.Rounded.CloudOff
                        is PlayerState.Playing -> Icons.Rounded.Pause
                        else -> Icons.Rounded.PlayArrow
                    },
                    "Play Button"
                )
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .requiredWidth(280.dp)
                .padding(10.dp)
        ) {
            // progress slider
            Slider(
                value = progressMillis.toFloat(),
                onValueChange = {
                    playerAction(PlayerAction.Pause)
                    progressMillis = it.toLong()
                },
                onValueChangeFinished = {
                    playerAction(PlayerAction.SeekTo(progressMillis))
                },
                valueRange = 0f..totalMillis.toFloat(),
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // passed duration
                DurationText(
                    progressDuration.toString(),
                    modifier = Modifier.weight(.25f)
                )

                // prev btn | todo: check if there is a previous chapter or disable this btn
                IconButton(
                    onClick = { playerAction(PlayerAction.Previous) },
                    modifier = Modifier.weight(.25f)
                        .height(25.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.NavigateBefore, "Previous")
                }

                // next btn | todo: check if there is a next chapter or disable this btn
                IconButton(
                    onClick = { playerAction(PlayerAction.Next) },
                    modifier = Modifier.weight(.25f).height(25.dp)
                ) {
                    Icon(Icons.AutoMirrored.Default.NavigateNext, "Next")
                }

                // total duration
                DurationText(
                    totalDuration.toString(),
                    TextAlign.End,
                    modifier = Modifier.weight(.25f)
                )
            }
        }
    }
}

@Composable
fun DurationText(duration: String, textAlign: TextAlign? = null, modifier: Modifier = Modifier) {
    Text(
        duration,
        fontFamily = FontFamily.Cursive,
        fontSize = 12.sp,
        textAlign = textAlign,
        modifier = modifier
    )
}