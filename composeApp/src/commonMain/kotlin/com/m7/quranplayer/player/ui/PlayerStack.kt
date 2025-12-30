package com.m7.quranplayer.player.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.core.ui.theme.Green
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
@Preview(showBackground = true, locale = "ar")
fun PlayerStackPreview_Playing() {
    PlayerStack(
        isSelected = { true },
        isPlaying = { true },
        playerState = { PlayerState.Playing(100, flowOf( 30)) },
        playerAction = {},
        isRepeatEnabled = { false },
        onRepeatClicked = {}
    )
}

@Composable
@Preview(showBackground = true)
fun PlayerStackPreview_Paused() {
    PlayerStack(
        isSelected = { true },
        isPlaying = { false },
        playerState = { PlayerState.Paused },
        playerAction = {},
        isRepeatEnabled = { false },
        onRepeatClicked = {}
    )
}

@Composable
fun PlayerStack(
    isSelected: () -> Boolean,
    isPlaying: () -> Boolean,
    playerState: () -> PlayerState,
    playerAction: (PlayerAction) -> Unit,
    isRepeatEnabled: () -> Boolean,
    onRepeatClicked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OptionsStack(isSelected(), modifier) {
        var totalMillis by remember { mutableLongStateOf(0) }
        val totalDuration = (totalMillis / 1000).toDuration(DurationUnit.SECONDS)

        var progressMillis by remember { mutableLongStateOf(0) }
        val progressDuration by remember {
            derivedStateOf {
                (progressMillis / 1000).toDuration(DurationUnit.SECONDS)
            }
        }

        val playIcon by rememberUpdatedState {
            when (playerState()) {
                is PlayerState.Playing -> Icons.Rounded.Pause
                PlayerState.Loading -> Icons.Rounded.Downloading
                else -> Icons.Rounded.PlayArrow
            }
        }

        val playIconColor by rememberUpdatedState {
            when (playerState()) {
                is PlayerState.Playing,
                PlayerState.Loading -> Orange

                is PlayerState.Error -> Color.Red

                else -> Green
            }
        }

        LaunchedEffect(playerState()) {
            Log("effect -> isSelected= ${isSelected()} - playerState= ${playerState()}")
            playerState().let {
                if (isSelected() && it is PlayerState.Playing) {
                    // update progress
                    totalMillis = it.duration
                    it.updatedPosition.collectLatest { progressMillis = it }
                }
            }
        }

        // slider
        OptionsRow(
            options = {
                ProgressSlider(
                    progressMillis = progressMillis.toFloat(),
                    totalMillis = totalMillis.toFloat(),
                    onProgressChange = {
                        Log("ProgressChange")
                        if (playerState != PlayerState.Paused) {
                            playerAction(PlayerAction.Pause)
                        }
                        progressMillis = it.toLong()
                    },
                    onProgressChangeFinished = {
                        Log("ProgressChangeFinished")
                        playerAction(PlayerAction.SeekTo(progressMillis))
                    },
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        )

        // duration
        OptionsRow(
            options = {
                // progress
                DurationText(
                    progressDuration.toString(),
                    modifier = Modifier
                        .weight(.5f)
                        .padding(horizontal = 10.dp)
                )

                // total
                DurationText(
                    totalDuration.toString(),
                    TextAlign.End,
                    modifier = Modifier
                        .weight(.5f)
                        .padding(horizontal = 10.dp)
                )
            }
        )

        // controls
        OptionsRow(
            modifier = Modifier.padding(bottom = 15.dp),
            options = {
                Row(
                    Modifier.weight(1f, false)
                        .padding(horizontal = 30.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(50))
                ) {
                    // prev
                    OutlinedIconButton({ playerAction(PlayerAction.Previous) }) {
                        Icon(Icons.AutoMirrored.Rounded.NavigateBefore, "Previous")
                    }

                    // play/pause
                    IconButton(
                        {
                            when {
                                isPlaying() -> playerAction(PlayerAction.Pause)
                                else -> playerAction(PlayerAction.Play)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors()
                            .copy(contentColor = playIconColor()),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = playIcon(),
                            contentDescription = "Play/Pause",
                            modifier = Modifier.aspectRatio(.9f)
                        )
                    }

                    // next
                    OutlinedIconButton({ playerAction(PlayerAction.Next) }) {
                        Icon(Icons.AutoMirrored.Rounded.NavigateNext, "Next")
                    }
                }

                // repeat
                IconToggleButton(
                    checked = isRepeatEnabled(),
                    onCheckedChange = onRepeatClicked,
                    modifier = Modifier
                        .padding(end = 30.dp)
                        .border(.5.dp, Color.LightGray, RoundedCornerShape(50))
                ) {
                    Icon(Icons.Rounded.RepeatOne, "Repeat")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSlider(
    progressMillis: Float,
    totalMillis: Float,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = progressMillis,
        valueRange = 0f..totalMillis,
        onValueChange = onProgressChange,
        onValueChangeFinished = onProgressChangeFinished,
        interactionSource = interactionSource,
        thumb = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(12.dp, 12.dp),
                )
            }
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(7.dp),
                thumbTrackGapSize = .5.dp
            )
        },
        modifier = modifier.height(20.dp)
    )
}

@Composable
fun DurationText(
    duration: String,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier
) {
    Text(
        duration,
        fontFamily = FontFamily.Cursive,
        fontSize = 10.sp,
        textAlign = textAlign,
        lineHeight = 15.sp,
        maxLines = 1,
        modifier = modifier
    )
}