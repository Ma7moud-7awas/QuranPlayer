package com.m7.quranplayer.player.data

import com.m7.quranplayer.core.Log
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.observationEnabled
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSKeyValueObservingOptionOld
import platform.Foundation.NSURL.Companion.URLWithString
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
class IOSPlayerSource() : PlayerSource {

    private var player: AVPlayer

    override val playerState: Channel<PlayerState> = Channel(CONFLATED)
    var currentItemUrl: String? = null

    var playerItemObserver = NSObject()
    var playerObserver = NSObject()

    init {
        AVPlayer.observationEnabled = true
        player = AVPlayer()

        playerObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                if (keyPath == "rate") {
                    Log("PlayerSource -> player: rate= ${player.rate}")
                    when {
                        player.rate > 0 -> checkItemStatus()

                        else -> {
                            if (player.currentItem?.status == AVPlayerItemStatusFailed) {
                                sendErrorState()
                            } else if (isItemTimeCompleted()) {
                                playerState.trySend(PlayerState.Ended)
                            } else {
                                playerState.trySend(PlayerState.Paused)
                            }
                        }
                    }
                }
            }
        }

        playerItemObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                if (keyPath == "status") {
                    checkItemStatus()
                }
            }
        }

        player.addObserver(
            playerObserver,
            "rate",
            NSKeyValueObservingOptionNew + NSKeyValueObservingOptionOld,
            null
        )
    }

    private fun checkItemStatus() {
        when (player.currentItem?.status) {
            AVPlayerItemStatusReadyToPlay -> {
                Log("PlayerSource -> item status= ${player.currentItem?.status} - ReadyToPlay")
                sendPlayState()
            }

            AVPlayerItemStatusFailed -> {
                Log("PlayerSource -> item status= ${player.currentItem?.status} - Failed")
                sendErrorState()
            }

            else -> { // handle unknown/buffering states
                Log("PlayerSource -> item status= ${player.currentItem?.status} - Unknown")
                playerState.trySend(PlayerState.Loading)
            }
        }
    }

    private fun sendPlayState() {
        playerState.trySend(
            PlayerState.Playing(
                duration = player.currentItem?.duration()?.toMilliseconds() ?: 0,
                updatedPosition = flow {
                    while (!isItemTimeCompleted()) {
                        delay(200)
                        emit(player.currentTime().toMilliseconds())
                    }
                }
            )
        )
    }

    private fun sendErrorState() {
        playerState.trySend(
            PlayerState.Error(
                Exception(
                    player.currentItem?.error?.localizedDescription
                        ?: player.error?.localizedDescription
                        ?: "Player Failed"
                )
            )
        )
    }

    private fun isItemTimeCompleted() =
        player.currentItem?.duration()?.toMilliseconds() == player.currentTime().toMilliseconds()

    private fun CValue<CMTime>.toMilliseconds(): Long {
        return (CMTimeGetSeconds(this) * 1000).toLong()
    }

    override fun setItem(url: String) {
        player.currentItem?.removeObserver(playerItemObserver, "status")

        URLWithString(url)?.also {
            currentItemUrl = url
            player.replaceCurrentItemWithPlayerItem(AVPlayerItem(it))

            player.currentItem?.addObserver(
                playerItemObserver,
                "status",
                NSKeyValueObservingOptionNew + NSKeyValueObservingOptionOld,
                null
            )
        }
    }

    override fun play(url: String) {
        if (currentItemUrl != url
            || player.currentItem == null
            || player.currentItem?.status == AVPlayerItemStatusFailed
        ) {
            // start new media item
            setItem(url)
        } else if (currentItemUrl == url && isItemTimeCompleted()) {
            // replay the current media item
            seekTo(0)
        }

        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seekToTime(CMTimeMake(value = positionMs, timescale = 1000))
        player.play()
    }

    override fun repeat() {
        seekTo(0)
        player.play()
    }

    override fun release() {
        player.currentItem?.removeObserver(
            playerItemObserver,
            "status",
            null
        )
        player.removeObserver(
            playerObserver,
            "rate",
            null
        )
        player.finalize()
        currentItemUrl = null
    }
}