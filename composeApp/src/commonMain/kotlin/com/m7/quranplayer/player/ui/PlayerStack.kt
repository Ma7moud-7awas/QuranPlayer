package com.m7.quranplayer.player.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.ui.theme.Green
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerStack(
    isSelected: Boolean,
    isPlaying: () -> Boolean,
    playerState: PlayerState,
    playerAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    OptionsStack(isSelected, modifier) {
        var totalMillis by remember { mutableLongStateOf(0) }
        val totalDuration = (totalMillis / 1000).toDuration(DurationUnit.SECONDS)

        var progressMillis by remember { mutableLongStateOf(0) }
        val progressDuration by remember {
            derivedStateOf {
                (progressMillis / 1000).toDuration(DurationUnit.SECONDS)
            }
        }

        var isRepeatEnabled by rememberSaveable { mutableStateOf(false) }

        val playIcon by rememberUpdatedState {
            when (playerState) {
                is PlayerState.Playing -> Icons.Rounded.Pause
                PlayerState.Loading -> Icons.Rounded.Downloading
                else -> Icons.Rounded.PlayArrow
            }
        }

        val playIconColor by rememberUpdatedState {
            when (playerState) {
                is PlayerState.Playing,
                PlayerState.Loading -> Orange

                is PlayerState.Error -> Color.Red

                else -> Green
            }
        }

        LaunchedEffect(playerState) {
            Log("playerState effect= $playerState, isSelected= $isSelected")
            if (isSelected) {
                when (playerState) {
                    is PlayerState.Playing -> {
                        // update progress
                        totalMillis = playerState.duration
                        playerState.updatedPosition.collectLatest { progressMillis = it }
                    }

                    is PlayerState.Ended -> {
                        if (isRepeatEnabled)
                            playerAction(PlayerAction.Repeat)
                        else
                            playerAction(PlayerAction.Next)
                    }

                    else -> Unit
                }
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                options = {
                    Row(
                        Modifier.weight(1f, false)
                            .padding(start = 30.dp, top = 5.dp, bottom = 5.dp, end = 30.dp)
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
                            Icon(playIcon(), "Play/Pause")
                        }

                        // next
                        OutlinedIconButton({ playerAction(PlayerAction.Next) }) {
                            Icon(Icons.AutoMirrored.Rounded.NavigateNext, "Next")
                        }
                    }

                    // repeat
                    IconToggleButton(
                        checked = isRepeatEnabled,
                        onCheckedChange = { isRepeatEnabled = it },
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .border(.5.dp, Color.LightGray, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Rounded.RepeatOne, "Repeat")
                    }
                }
            )
        }
    }
}