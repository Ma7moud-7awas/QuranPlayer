package com.m7.quranplayer.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.m7.quranplayer.core.di.initKoin
import com.m7.quranplayer.core.ui.App
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSUserDefaults
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingPlaybackStatePaused
import platform.MediaPlayer.MPNowPlayingPlaybackStatePlaying
import platform.MediaPlayer.MPNowPlayingPlaybackStateStopped
import platform.MediaPlayer.MPNowPlayingPlaybackStateUnknown
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusCommandFailed
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.saad_al_ghamdy

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    initKoin()

    AVAudioSession.sharedInstance().apply {
        setCategory(AVAudioSessionCategoryPlayback, null)
        setActive(true, null)
    }

    UIApplication.sharedApplication().beginReceivingRemoteControlEvents()

    var playerCenterAction by remember { mutableStateOf<PlayerAction?>(null) }
    var currentChapterId: String? by remember { mutableStateOf(null) }

    handleCenterCommands(currentChapterId) { playerCenterAction = it }

    val mediaCenter = MPNowPlayingInfoCenter.defaultCenter()
    val reciterName = stringResource(Res.string.saad_al_ghamdy)
    val scope = rememberCoroutineScope()

    App(
        onLanguageChanged = { changeLanguage(it) },
        playerCenterAction = { playerCenterAction },
        onStateChanged = { playerState, chapter ->
            currentChapterId = chapter?.id

            mediaCenter.playbackState =
                when (playerState) {
                    PlayerState.Loading -> MPNowPlayingPlaybackStateUnknown
                    is PlayerState.Playing -> {
                        mediaCenter.nowPlayingInfo =
                            mapOf(
                                MPMediaItemPropertyArtist to reciterName,
                                MPMediaItemPropertyTitle to "${chapter?.title}",
                                MPMediaItemPropertyPlaybackDuration to playerState.duration / 1000,
                            )

                        scope.launch {
                            playerState.updatedPosition.collectLatest { elapsedTime ->
                                mediaCenter.nowPlayingInfo = mediaCenter.nowPlayingInfo?.let {
                                    it + (MPNowPlayingInfoPropertyElapsedPlaybackTime to elapsedTime / 1000)
                                }
                            }
                        }

                        MPNowPlayingPlaybackStatePlaying
                    }

                    PlayerState.Paused -> MPNowPlayingPlaybackStatePaused
                    PlayerState.Ended -> MPNowPlayingPlaybackStateStopped
                    else -> MPNowPlayingPlaybackStateStopped
                }
        }
    )
}

private fun changeLanguage(langCode: String) {
    NSUserDefaults.standardUserDefaults
        .setObject(listOf(langCode), "AppleLanguages")
}

private fun handleCenterCommands(chapterId: String?, onCommand: (PlayerAction) -> Unit) {
    MPRemoteCommandCenter.sharedCommandCenter().apply {
        playCommand.addTargetWithHandler {
            onCommand(PlayerAction.Play)
            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
        }
        pauseCommand.addTargetWithHandler {
            onCommand(PlayerAction.Pause)
            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
        }
        nextTrackCommand.addTargetWithHandler {
            onCommand(
                chapterId?.let { PlayerAction.Next.WithId(chapterId) } ?: PlayerAction.Next
            )
            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
        }
        previousTrackCommand.addTargetWithHandler {
            onCommand(
                chapterId?.let { PlayerAction.Previous.WithId(chapterId) } ?: PlayerAction.Previous
            )
            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
        }
        changePlaybackPositionCommand.addTargetWithHandler { event ->
            onCommand(
                (event as? MPChangePlaybackPositionCommandEvent)
                    ?.positionTime?.toLong()
                    ?.let { PlayerAction.SeekTo(it * 1000) }
                    ?: return@addTargetWithHandler MPRemoteCommandHandlerStatusCommandFailed
            )

            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
        }
    }
}
