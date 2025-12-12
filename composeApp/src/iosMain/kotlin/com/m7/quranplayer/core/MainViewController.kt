package com.m7.quranplayer.core

import androidx.compose.ui.window.ComposeUIViewController
import com.m7.quranplayer.core.di.initKoin
import com.m7.quranplayer.core.ui.App
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingPlaybackStateInterrupted
import platform.MediaPlayer.MPNowPlayingPlaybackStatePaused
import platform.MediaPlayer.MPNowPlayingPlaybackStatePlaying
import platform.MediaPlayer.MPNowPlayingPlaybackStateStopped
import platform.MediaPlayer.MPNowPlayingPlaybackStateUnknown
import platform.UIKit.UIApplication
import platform.UIKit.beginReceivingRemoteControlEvents

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    initKoin()

    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
    AVAudioSession.sharedInstance().setActive(true, null)
    UIApplication.sharedApplication().beginReceivingRemoteControlEvents()

    val playingCenter = MPNowPlayingInfoCenter.defaultCenter()

    // todo: handle center control events
//    MPRemoteCommandCenter.sharedCommandCenter().apply {
//        playCommand.addTargetWithHandler {
//            return@addTargetWithHandler MPRemoteCommandHandlerStatusSuccess
//        }
//    }

    App { playerState ->
        playingCenter.playbackState = when (playerState) {
            PlayerState.Loading -> MPNowPlayingPlaybackStateUnknown
            is PlayerState.Playing -> {
                playingCenter.nowPlayingInfo =
                    mapOf(
                        MPMediaItemPropertyTitle to "Quran Player",
                        MPMediaItemPropertyArtist to "Saad Al Ghamdy",
                        MPMediaItemPropertyPlaybackDuration to playerState.duration / 1000
                    )

                MPNowPlayingPlaybackStatePlaying
            }

            PlayerState.Paused -> MPNowPlayingPlaybackStatePaused
            PlayerState.Ended -> MPNowPlayingPlaybackStateStopped
            else -> MPNowPlayingPlaybackStateInterrupted
        }
    }
}