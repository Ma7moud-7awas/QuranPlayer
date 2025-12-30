package com.m7.quranplayer.player.data

import com.m7.quranplayer.core.Log
import com.m7.quranplayer.downloader.data.DownloadManager
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

    private var currentItemId: String? = null
    private var playerItemObserver = NSObject()
    private var playerObserver = NSObject()

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
        return (this.toSeconds() * 1000).toLong()
    }

    private fun CValue<CMTime>.toSeconds(): Double =
        CMTimeGetSeconds(this)

    fun setItem(id: String) {
        player.currentItem?.removeObserver(playerItemObserver, "status")

        URLWithString(DownloadManager.getDownloadUrl(id))?.also {
            currentItemId = id
            player.replaceCurrentItemWithPlayerItem(AVPlayerItem(it))

            player.currentItem?.addObserver(
                playerItemObserver,
                "status",
                NSKeyValueObservingOptionNew + NSKeyValueObservingOptionOld,
                null
            )
        }
    }

    override suspend fun play(id: String, title: String) {
        if (currentItemId != id
            || player.currentItem == null
            || player.currentItem?.status == AVPlayerItemStatusFailed
        ) {
            // start new media item
            setItem(id)
        } else if (currentItemId == id && isItemTimeCompleted()) {
            // replay the current media item
            seekTo(0)
        }

        player.play()
    }

    override suspend fun pause() {
        player.pause()
    }

    override suspend fun seekTo(positionMs: Long) {
        player.seekToTime(CMTimeMake(value = positionMs, timescale = 1000))
        player.play()
    }

    override suspend fun repeat() {
        seekTo(0)
        player.play()
    }

    override suspend fun release() {
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
        currentItemId = null
    }
}