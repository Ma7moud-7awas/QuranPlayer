package com.m7.quranplayer.player.data

import com.m7.quranplayer.core.Log
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.foundation.NSKeyValueObservingProtocol

@OptIn(ExperimentalForeignApi::class)
class IOSPlayerSource(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : PlayerSource {

    private var player: AVQueuePlayer? = null

    override val playerState: Channel<Pair<Int, PlayerState>> = Channel(CONFLATED)

    override val playerAction: Channel<PlayerAction> = Channel(CONFLATED)

    private var playerItems: List<PlayerItem> = listOf()
    private var currentItemIndex = -1
    private var repeatMode = RepeatMode.None
    private var callingNext = false

    private var playerRateObserver = NSObject()
    private var itemStatusObserver = NSObject()
    private var currentItemObserver = NSObject()

    init {
        scope.launch {
            AVQueuePlayer.observationEnabled = true
            initObservers()

            MediaCenterManager.handleCenterCommands { playerAction.trySend(it) }
        }
    }

    private fun initObservers() {
        playerRateObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                scope.launch {
                    if (keyPath == "rate") {
                        player?.also {
                            Log("RateObserver -> rate= ${it.rate}")
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
        }

        itemStatusObserver = object : NSObject(), NSKeyValueObservingProtocol {
            override fun observeValueForKeyPath(
                keyPath: String?,
                ofObject: Any?,
                change: Map<Any?, *>?,
                context: COpaquePointer?
            ) {
                scope.launch {
                    Log("StatusObserver -> status= $change")
                    if (keyPath == "status") {
                        checkItemStatus()
                    }
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
                scope.launch {
                    Log("currentItemObserver -> currentItem= $change")
                    if (keyPath == "currentItem") {
                        (change?.get(NSKeyValueChangeNewKey) as? AVPlayerItem)
                            .let { newItem ->
                                player?.pause()

                                newItem?.toPlayerItem()?.let {
                                    // updating index
                                    currentItemIndex = playerItems.indexOf(it)
                                    // add observer to the new item
                                    addItemStatusObserver(newItem)

                                    if (!callingNext && repeatMode == RepeatMode.One) {
                                        // reselect & play the previous item
                                        previous()
                                    } else {
                                        callingNext = false
                                        // play the new item
                                        player?.play()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun addObservers() {
        scope.launch {
            player?.let {
                it.addObserver(
                    playerRateObserver,
                    "rate",
                    NSKeyValueObservingOptionNew,
                    null
                )

                it.addObserver(
                    currentItemObserver,
                    "currentItem",
                    NSKeyValueObservingOptionNew,
                    null
                )

                addItemStatusObserver(it.currentItem)
            }
        }
    }

    private fun addItemStatusObserver(item: AVPlayerItem?) {
        Log("addItemStatusObserver ->")
        item?.addObserver(
            itemStatusObserver,
            "status",
            NSKeyValueObservingOptionNew,
            null
        )
    }

    private fun removeObservers() {
        player?.also {
            it.removeObserver(playerRateObserver, "rate")
            it.removeObserver(currentItemObserver, "currentItem")
            it.currentItem?.removeObserver(itemStatusObserver, "status")
        }
    }

    private fun checkItemStatus() {
        when (player?.currentItem?.status) {
            AVPlayerItemStatusReadyToPlay -> {
                Log("checkItemStatus -> status= ${player?.currentItem?.status} - ReadyToPlay")
                if (currentItemIndex != -1)
                    sendPlayState()
            }

            AVPlayerItemStatusFailed -> {
                Log("checkItemStatus -> status= ${player?.currentItem?.status} - Failed")
                player?.removeAllItems()
                sendErrorState()
            }

            else -> { // handle unknown/buffering states
                Log("checkItemStatus -> status= ${player?.currentItem?.status} - Unknown")
                sendState(PlayerState.Loading)
            }
        }
    }

    private fun sendState(state: PlayerState) {
        Log("sendState -> state = $state - idx = $currentItemIndex")
        playerState.trySend(currentItemIndex to state)
        MediaCenterManager.bindCenterInfo(state, playerItems[currentItemIndex].title)
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

    override suspend fun setPlaylist(items: List<PlayerItem>) {
        scope.launch {
            playerItems = items
        }
    }

    override suspend fun play(selectedIndex: Int) {
        scope.launch {
            if (selectedIndex != currentItemIndex)
                getQueueByStartIndex(selectedIndex).let {
                    currentItemIndex = selectedIndex
                    resetPlayer(it)
                }

            player?.play()
        }
    }

    private fun getQueueByStartIndex(idx: Int): List<PlayerItem> {
        return playerItems.subList(idx, playerItems.size)
    }

    private fun resetPlayer(items: List<PlayerItem>) {
        removeObservers()
        player?.removeAllItems()

        player = AVQueuePlayer(items.map { it.toAVPlayerItem() })
        addObservers()
    }

    override suspend fun pause() {
        scope.launch {
            player?.pause()
        }
    }

    override suspend fun previous() {
        if (currentItemIndex > 0)
            play(currentItemIndex - 1)
    }

    override suspend fun next() {
        scope.launch {
            callingNext = true
            player?.advanceToNextItem()
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        scope.launch {
            player?.also {
                it.seekToTime(CMTimeMake(value = positionMs, timescale = 1000))
                it.play()
            }
        }
    }

    override suspend fun enableRepeat(enable: Boolean) {
        scope.launch {
            repeatMode = if (enable) RepeatMode.One else RepeatMode.None
        }
    }

    override suspend fun release() {
        scope.launch {
            removeObservers()
            player?.finalize()
            player = null
        }
    }
}