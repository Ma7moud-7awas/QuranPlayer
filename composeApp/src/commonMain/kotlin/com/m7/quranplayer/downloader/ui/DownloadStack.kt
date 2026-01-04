package com.m7.quranplayer.downloader.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.chapter.ui.chaptersFakeList
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.ui.OptionsRow
import com.m7.quranplayer.core.ui.OptionsStack
import com.m7.quranplayer.downloader.domain.model.DownloadState
import com.m7.quranplayer.downloader.domain.model.DownloaderAction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.chapter_added_to_queue
import quranplayer.composeapp.generated.resources.chapter_is_available_offline
import quranplayer.composeapp.generated.resources.could_not_download_chapter
import quranplayer.composeapp.generated.resources.download_chapter_to_play_offline

@Composable
@Preview(showBackground = true, locale = "ar")
@Preview(showBackground = true)
private fun DownloadStack_NotDownloaded() {
    val chapter = chaptersFakeList.first()
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, locale = "ar")
@Preview(showBackground = true)
private fun DownloadStack_Queued() {
    val chapter = chaptersFakeList.first().copy(downloadState = DownloadState.Queued)
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, locale = "ar")
@Preview(showBackground = true)
private fun DownloadStack_Error() {
    val chapter = chaptersFakeList.first()
        .copy(downloadState = DownloadState.Error(Exception()))
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, locale = "ar")
@Preview(showBackground = true)
private fun DownloadStack_Completed() {
    val chapter = chaptersFakeList.first()
        .copy(downloadState = DownloadState.Completed)
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, locale = "ar")
private fun DownloadStack_Paused() {
    val chapter = chaptersFakeList.first()
        .copy(downloadState = DownloadState.Paused(.2f))
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, locale = "ar")
private fun DownloadStack_Downloading() {
    val chapter = chaptersFakeList.first()
        .copy(downloadState = DownloadState.Downloading(flowOf(.5f)))
    DownloadStack(
        { chapter },
        expanded = { true },
        downloaderAction = { _, _ -> }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadStack(
    chapter: () -> Chapter,
    expanded: () -> Boolean,
    downloaderAction: (String, DownloaderAction) -> Unit,
    modifier: Modifier = Modifier
) {
    OptionsStack(expanded(), modifier) {
        var progress by remember { mutableFloatStateOf(0f) }
        val amplitude by rememberUpdatedState {
            derivedStateOf {
                if (chapter().downloadState is DownloadState.Downloading) .3f else .0f
            }
        }

        val hintStringRes = when (chapter().downloadState) {
            is DownloadState.Completed -> Res.string.chapter_is_available_offline
            is DownloadState.Queued -> Res.string.chapter_added_to_queue
            is DownloadState.Error -> Res.string.could_not_download_chapter
            else -> Res.string.download_chapter_to_play_offline
        }

        val startIcon = when (chapter().downloadState) {
            is DownloadState.Completed -> Icons.Rounded.Delete
            is DownloadState.Queued -> Icons.Rounded.LineWeight
            is DownloadState.Downloading -> Icons.Rounded.Pause
            is DownloadState.Error -> Icons.Rounded.Error
            else -> Icons.Rounded.Download
        }

        LaunchedEffect(chapter().downloadState) {
            Log("effect downloadState= ${chapter().downloadState}")
            chapter().downloadState.also { state ->
                // update progress
                if (expanded() && state is DownloadState.Downloading) {
                    state.updatedPosition.collectLatest { progress = it }

                } else if (expanded() && state is DownloadState.Paused) {
                    progress = state.downloadedPercent
                }
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            OptionsRow(
                modifier
                    .height(45.dp)
                    .padding(vertical = 10.dp)
            ) {
                when (chapter().downloadState) {
                    is DownloadState.NotDownloaded,
                    is DownloadState.Queued,
                    is DownloadState.Error,
                    is DownloadState.Completed -> {
                        // hint
                        Text(
                            text = stringResource(hintStringRes),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(.8f).basicMarquee()
                        )
                    }

                    else -> {
                        // stop btn
                        OutlinedIconButton(
                            border = BorderStroke(.5.dp, Color.Gray),
                            colors = IconButtonDefaults.iconButtonColors()
                                .copy(contentColor = Color.DarkGray),
                            onClick = {
                                downloaderAction(chapter().id, DownloaderAction.Stop)
                            }) {
                            Icon(Icons.Rounded.Stop, "Stop download")
                        }

                        // progress
                        LinearWavyProgressIndicator(
                            progress = { progress },
                            amplitude = { amplitude().value },
                            wavelength = 20.dp,
                            modifier = Modifier.fillMaxWidth(.8f)
                        )
                    }
                }

                // start btn
                OutlinedIconButton(
                    border = BorderStroke(.5.dp, Color.Gray),
                    colors = IconButtonDefaults.iconButtonColors()
                        .copy(contentColor = Color.DarkGray),
                    modifier = Modifier.weight(1f, fill = false),
                    onClick = {
                        chapter().let {
                            when (it.downloadState) {
                                is DownloadState.Completed ->
                                    downloaderAction(it.id, DownloaderAction.Stop)

                                is DownloadState.Downloading ->
                                    downloaderAction(it.id, DownloaderAction.Pause)

                                else -> downloaderAction(it.id, DownloaderAction.Start)
                            }
                        }
                    }
                ) {
                    Icon(startIcon, "Start/Pause download")
                }
            }
        }
    }
}