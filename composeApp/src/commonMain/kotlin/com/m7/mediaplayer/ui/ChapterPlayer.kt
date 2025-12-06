package com.m7.mediaplayer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.mediaplayer.Log
import com.m7.mediaplayer.model.Chapter
import com.m7.mediaplayer.ui.theme.Green
import com.m7.mediaplayer.ui.theme.Orange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
@Preview(locale = "")
fun ChapterPlayerPreview() {
    ChapterPlayer(
        Chapter(1, "Al Fatiha", 10),
        true,
        {},
        {},
        {},
        {}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChapterPlayer(
    chapter: Chapter?,
    expanded: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (chapter?.durationSeconds ?: 0)

    val totalDuration = totalSeconds.toDuration(DurationUnit.SECONDS)

    var passedSeconds by remember { mutableIntStateOf(0) }

    val passedDuration = rememberUpdatedState { passedSeconds.toDuration(DurationUnit.SECONDS) }

    val progress = rememberUpdatedState { passedSeconds / totalSeconds.toFloat() }

    val animatedProgress by animateFloatAsState(
        progress.value(),
        spring(stiffness = 5f)
    )

    val fabBgColor by animateColorAsState(targetValue = if (expanded) Orange else Green)

    val scope = rememberCoroutineScope()

    // play new selected chapter
    LaunchedEffect(chapter?.id) {
        Log("selection effect")
        chapter?.id?.also {
            // reset the passed duration to start from the beginning
            passedSeconds = 0
            scope.startTimer(
                totalSeconds = totalSeconds,
                passedSeconds = passedSeconds,
                onTick = {
                    passedSeconds++
                },
                onComplete = {
                    Log("selection onComplete")
                    passedSeconds = 0
                    onNext()
                }
            )
        }
    }

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = expanded,
        floatingActionButton = {
            // play/pause btn
            FloatingToolbarDefaults.StandardFloatingActionButton(
                containerColor = fabBgColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    if (expanded) {
                        Log("pause action")
                        onPause()
                        scope.endTimer()
                    } else {
                        Log("play action")
                        onPlay()
                        chapter?.id?.also {
                            scope.startTimer(
                                totalSeconds = totalSeconds,
                                passedSeconds = passedSeconds,
                                onTick = {
                                    passedSeconds++
                                },
                                onComplete = {
                                    Log("play onComplete")
                                    passedSeconds = 0
                                    onNext()
                                }
                            )
                        }
                    }
                }
            ) {
                Icon(
                    if (expanded) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    "Play"
                )
            }
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // progress bar
            LinearWavyProgressIndicator(
                progress = { animatedProgress },
                amplitude = { .25f },
                wavelength = 25.dp,
                waveSpeed = 5.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // passed duration
                DurationText(passedDuration.value().toString())

                // prev btn | todo: check if there is a previous chapter or disable this btn
                IconButton(onClick = { onPrevious() }, Modifier) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }

                // next btn | todo: check if there is a next chapter or disable this btn
                IconButton(onClick = { onNext() }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }

                // total duration
                DurationText(totalDuration.toString())
            }
        }
    }
}

fun CoroutineScope.startTimer(
    totalSeconds: Int,
    passedSeconds: Int,
    onTick: () -> Unit,
    onComplete: () -> Unit
) {
    val childJob = launch(start = CoroutineStart.LAZY) {
        // continue from the passed duration
        repeat(totalSeconds - passedSeconds) {
            delay(1000)
            // deliver to reflect the progress
            onTick()
        }

        onComplete()
    }

    // cancel previously started jobs
    coroutineContext.job.children.forEach {
        if (it != childJob) {
            Log("job: $it canceled")
            it.cancel()
        }
    }

    childJob.start()
}

fun CoroutineScope.endTimer() {
    // cancel the timer jobs
    coroutineContext.job.cancelChildren()
}

@Composable
fun DurationText(duration: String, modifier: Modifier = Modifier) {
    Text(
        duration,
        fontFamily = FontFamily.Cursive,
        fontSize = 12.sp,
        modifier = modifier
    )
}