package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPChangeRepeatModeCommandEvent
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
import platform.MediaPlayer.MPRepeatType
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.saad_al_ghamdy

object MediaCenterManager {

    val mediaCenter = MPNowPlayingInfoCenter.defaultCenter()
    val scope = CoroutineScope(Dispatchers.Default)

    init {
        UIApplication.sharedApplication().beginReceivingRemoteControlEvents()
    }

    fun bindCenterInfo(playerState: PlayerState, title: String?) {
        scope.launch {
            mediaCenter.playbackState =
                when (playerState) {
                    PlayerState.Loading -> MPNowPlayingPlaybackStateUnknown
                    is PlayerState.Playing -> {
                        mediaCenter.nowPlayingInfo =
                            mapOf(
                                MPMediaItemPropertyTitle to title,
                                MPMediaItemPropertyArtist to getString(Res.string.saad_al_ghamdy),
                                MPMediaItemPropertyPlaybackDuration to playerState.duration / 1000,
                            )

                        playerState.updatedPosition.collectLatest { elapsedTime ->
                            mediaCenter.nowPlayingInfo = mediaCenter.nowPlayingInfo?.let {
                                it + (MPNowPlayingInfoPropertyElapsedPlaybackTime to elapsedTime / 1000)
                            }
                        }

                        MPNowPlayingPlaybackStatePlaying
                    }

                    PlayerState.Paused -> MPNowPlayingPlaybackStatePaused
                    PlayerState.Ended -> MPNowPlayingPlaybackStateStopped
                    else -> MPNowPlayingPlaybackStateStopped
                }
        }
    }

    fun handleCenterCommands(playerAction: (PlayerAction) -> Unit) {
        MPRemoteCommandCenter.sharedCommandCenter().apply {
            playCommand.addTargetWithHandler {
                playerAction(PlayerAction.Play)
                return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
            }
            pauseCommand.addTargetWithHandler {
                playerAction(PlayerAction.Pause)
                return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
            }
            nextTrackCommand.addTargetWithHandler {
                playerAction(PlayerAction.Next)
                return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
            }
            previousTrackCommand.addTargetWithHandler {
                playerAction(PlayerAction.Previous)
                return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
            }
            changeRepeatModeCommand.addTargetWithHandler { event ->
                (event as? MPChangeRepeatModeCommandEvent)
                    ?.let {
                        playerAction(PlayerAction.Repeat(it.repeatType == MPRepeatType.MPRepeatTypeOne))
                        return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
                    }

                return@addTargetWithHandler MPRemoteCommandHandlerStatusCommandFailed
            }
            changePlaybackPositionCommand.addTargetWithHandler { event ->
                (event as? MPChangePlaybackPositionCommandEvent)
                    ?.positionTime?.toLong()
                    ?.let {
                        playerAction(PlayerAction.SeekTo(it * 1000))
                        return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
                    }

                return@addTargetWithHandler MPRemoteCommandHandlerStatusCommandFailed
            }
        }
    }
}