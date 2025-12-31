package com.m7.quranplayer.player.data

import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.Log.log
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVQueuePlayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.observationEnabled
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSKeyValueObservingOptionOld
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
class IOSPlayerSource() : PlayerSource {

    private var player: AVQueuePlayer? = null

    override val playerState: Channel<Pair<String?, PlayerState>> = Channel(CONFLATED)

    override val playerAction: Channel<PlayerAction> = Channel(CONFLATED)

    private var currentItem: PlayerItem? = null
    private var playerRateObserver = NSObject()
    private var itemStatusObserver = NSObject()
    private var currentItemObserver = NSObject()

    init {
        AVQueuePlayer.observationEnabled = true
        initObservers()

        MediaCenterManager.handleCenterCommands { playerAction.trySend(it) }
    }

    private fun sendState(state: PlayerState) {
        playerState.trySend(currentItem?.id to state)
        MediaCenterManager.bindCenterInfo(state, currentItem?.title)
    }

    private fun initObservers() {
        playerRateObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                if (keyPath == "rate") {
                    player?.also {
                        Log("PlayerSource -> player: rate= ${it.rate}")
                        when {
                            it.rate > 0 -> checkItemStatus()

                            else -> {
                                if (it.currentItem?.status == AVPlayerItemStatusFailed) {
                                    sendErrorState()

                                } else if (player?.currentItem?.isItemTimeCompleted() ?: true) {
                                    sendState(PlayerState.Ended)

                                } else {
                                    sendState(PlayerState.Paused)
                                }
                            }
                        }
                    }
                }
            }
        }

        itemStatusObserver = object : NSObject(), NSKeyValueObservingProtocol {
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

        currentItemObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                if (keyPath == "currentItem") {
                    (change?.get(NSKeyValueChangeNewKey) as? AVPlayerItem)?.let { newItem ->
                        newItem.getId().also { id ->
                            id.log("new id = ")
                            if (id != currentItem?.id) {
                                currentItem = newItem.toPlayerItem()
                                sendPlayState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addObservers() {
        player?.addObserver(
            playerRateObserver,
            "rate",
            NSKeyValueObservingOptionNew + NSKeyValueObservingOptionOld,
            null
        )

        player?.currentItem?.addObserver(
            itemStatusObserver,
            "status",
            NSKeyValueObservingOptionNew + NSKeyValueObservingOptionOld,
            null
        )

        player?.addObserver(
            currentItemObserver,
            "currentItem",
            NSKeyValueObservingOptionNew,
            null
        )
    }

    private fun removeObservers() {
        player?.also {
            it.currentItem?.removeObserver(itemStatusObserver, "status")
            it.removeObserver(currentItemObserver, "currentItem")
            it.removeObserver(playerRateObserver, "rate")
        }
    }

    private fun checkItemStatus() {
        when (player?.currentItem?.status) {
            AVPlayerItemStatusReadyToPlay -> {
                Log("PlayerSource -> item status= ${player?.currentItem?.status} - ReadyToPlay")
                sendPlayState()
            }

            AVPlayerItemStatusFailed -> {
                Log("PlayerSource -> item status= ${player?.currentItem?.status} - Failed")
                player?.removeAllItems()
                sendErrorState()
            }

            else -> { // handle unknown/buffering states
                Log("PlayerSource -> item status= ${player?.currentItem?.status} - Unknown")
                sendState(PlayerState.Loading)
            }
        }
    }

    private fun sendPlayState() {
        sendState(
            PlayerState.Playing(
                duration = player?.currentItem?.duration()?.toMilliseconds() ?: 0,
                updatedPosition = flow {
                    player?.also {
                        while (!(player?.currentItem?.isItemTimeCompleted() ?: true)) {
                            delay(200)
                            emit(it.currentTime().toMilliseconds())
                        }
                    }
                }
            )
        )
    }

    private fun sendErrorState() {
        sendState(
            PlayerState.Error(
                Exception(
                    player?.currentItem?.error?.localizedDescription
                        ?: player?.error?.localizedDescription
                        ?: "Player Failed"
                )
            )
        )
    }

    private fun setItems(items: List<PlayerItem>) {
        player?.removeAllItems()
        removeObservers()

        val avItems = items.map { it.toAVPlayerItem() }
        player = AVQueuePlayer(avItems)

        addObservers()
    }

    override suspend fun play(items: List<PlayerItem>) {
        val firstItem = items.firstOrNull() ?: return

        if (firstItem.id != player?.currentItem?.getId()) {
            // start new queue
            setItems(items)
            currentItem = firstItem
        }

        player?.play()
    }

    override suspend fun pause() {
        player?.pause()
    }

    override suspend fun previous(items: List<PlayerItem>) {
        play(items)
    }

    override suspend fun next() {
        player?.advanceToNextItem()
    }

    override suspend fun seekTo(positionMs: Long) {
        player?.also {
            it.seekToTime(CMTimeMake(value = positionMs, timescale = 1000))
            it.play()
        }
    }

    override suspend fun repeat() {
        seekTo(0)
    }

    override suspend fun release() {
        removeObservers()
        player?.finalize()
        currentItem = null
    }
}