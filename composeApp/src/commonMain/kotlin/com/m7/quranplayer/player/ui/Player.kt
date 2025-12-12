package com.m7.quranplayer.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOn
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.RepeatOneOn
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.ui.theme.Green
import com.m7.quranplayer.core.ui.theme.QuranPlayerTheme
import com.m7.quranplayer.core.ui.theme.Orange
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
@Preview(locale = "ar")
fun PlayerPreview() {
    QuranPlayerTheme {
        Player(
//            PlayerState.Ended,
//        PlayerState.Paused,
            PlayerState.Playing(10, flowOf(5)),
//        PlayerState.Error(Exception()),
//        PlayerState.Loading,
            playerAction = {}
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Player(
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

    val expanded = remember(playerState) {
        playerState.let {
            it is PlayerState.Playing || it is PlayerState.Paused || it is PlayerState.Loading
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

    var isRepeatEnabled by rememberSaveable { mutableStateOf(false) }

    // update progress ui state when player state changes
    LaunchedEffect(playerState) {
        Log("playerState effect = $playerState")
        when (playerState) {
            is PlayerState.Playing -> {
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

    Column(modifier = modifier) {
        // progress bar
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .dropShadow(
                    RoundedCornerShape(35),
                    Shadow(15.dp, alpha = .3f)
                )
        ) {
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
                modifier = Modifier
                    .requiredWidth(288.dp)
                    .clip(RoundedCornerShape(35))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(5.dp)
            )
        }

        // controllers
        HorizontalFloatingToolbar(
            modifier = Modifier,
            shape = RoundedCornerShape(25),
            expandedShadowElevation = 20.dp,
            contentPadding = PaddingValues(horizontal = 5.dp),
            expanded = expanded,
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
                            is PlayerState.Error -> Icons.Rounded.WifiOff
                            is PlayerState.Playing -> Icons.Rounded.Pause
                            else -> Icons.Rounded.PlayArrow
                        },
                        "Play Button"
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .requiredWidth(280.dp)
                        .padding(horizontal = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // passed duration
                    DurationText(
                        progressDuration.toString(),
                        modifier = Modifier
                            .weight(.3f)
                            .padding(horizontal = 5.dp)
                    )

                    // prev/next controls
                    Row(
                        Modifier.weight(.4f)
                            .border(1.dp, Color.Gray, RoundedCornerShape(50))
                    ) {
                        // prev btn | todo: check if there is a previous chapter or disable this btn
                        IconButton(
                            onClick = { playerAction(PlayerAction.Previous) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.NavigateBefore, "Previous")
                        }

                        // repeat btn
                        IconToggleButton(
                            checked = isRepeatEnabled,
                            onCheckedChange = { isRepeatEnabled = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.RepeatOne, "Previous")
                        }

                        // next btn | todo: check if there is a next chapter or disable this btn
                        IconButton(
                            onClick = { playerAction(PlayerAction.Next) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.NavigateNext, "Next")
                        }
                    }

                    // total duration
                    DurationText(
                        totalDuration.toString(),
                        TextAlign.End,
                        modifier = Modifier
                            .weight(.3f)
                            .padding(horizontal = 5.dp)
                    )
                }
            }
        }
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                        thumbSize = DpSize(10.dp, 10.dp),
                    )
                }
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(5.dp),
                    thumbTrackGapSize = .5.dp
                )
            },
            modifier = modifier.height(15.dp)
        )
    }
}

@Composable
fun DurationText(duration: String, textAlign: TextAlign? = null, modifier: Modifier = Modifier) {
    Text(
        duration,
        fontFamily = FontFamily.Cursive,
        autoSize = TextAutoSize.StepBased(12.sp, 16.sp),
        textAlign = textAlign,
        maxLines = 1,
        modifier = modifier
    )
}