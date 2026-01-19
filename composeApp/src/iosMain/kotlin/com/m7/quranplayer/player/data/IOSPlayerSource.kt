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
import platform.AVFoundation.AVPlayerActionAtItemEndNone
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
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
import platform.AVFoundation.setActionAtItemEnd
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSKeyValueChangeNewKey
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSNotificationCenter
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.darwin.NSObjectProtocol
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

    private val notificationCenter = NSNotificationCenter.defaultCenter
    private var playToEndObserver: NSObjectProtocol? = null
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
                            Log("onRateChanged -> rate= ${it.rate}")
                            when {
                                it.rate > 0 -> checkItemStatus()

                                else -> {
                                    if (it.currentItem?.status == AVPlayerItemStatusFailed) {
                                        sendErrorState()

                                    } else if (player?.currentItem?.isItemTimeCompleted() == true) {
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
                    Log("onStatusChanged -> status= $change")
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
                    Log("onCurrentItemChanged -> currentItem= $change")
                    if (keyPath == "currentItem") {
                        (change?.get(NSKeyValueChangeNewKey) as? AVPlayerItem)?.let { newItem ->
                            newItem.toPlayerItem()?.let {
                                // updating index
                                currentItemIndex = playerItems.indexOf(it)

                                // add observers to the new item
                                addItemStatusObserver(newItem)
                                addItemPlayToEndObserver(newItem)

                                // start
                                player?.play()
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

                addItemPlayToEndObserver(it.currentItem)
            }
        }
    }

    private fun addItemStatusObserver(item: AVPlayerItem?) {
        Log("addItemStatusObserver")
        item?.addObserver(
            itemStatusObserver,
            "status",
            NSKeyValueObservingOptionNew,
            null
        )
    }

    private fun addItemPlayToEndObserver(item: AVPlayerItem?) {
        Log("addItemPlayToEndObserver")
        playToEndObserver = notificationCenter.addObserverForName(
            AVPlayerItemDidPlayToEndTimeNotification,
            item,
            null
        ) { notification ->
            scope.launch {
                Log("onPlayToEnd -> notification= $notification")
                if (repeatMode == RepeatMode.One) {
                    // replay
                    seekTo(0)

                } else if (currentItemIndex == playerItems.lastIndex) {
                    // stopped
                    sendState(PlayerState.Ended)

                } else {
                    // advance
                    player?.advanceToNextItem()
                }
            }
        }
    }

    private fun removeObservers() {
        player?.also {
            it.removeObserver(playerRateObserver, "rate")
            it.currentItem?.removeObserver(itemStatusObserver, "status")
            it.removeObserver(currentItemObserver, "currentItem")
        }
        playToEndObserver?.also { notificationCenter.removeObserver(it) }
    }

    private fun checkItemStatus() {
        when (val status = player?.currentItem?.status) {
            AVPlayerItemStatusReadyToPlay -> {
                Log("checkItemStatus -> status= $status - ReadyToPlay")
                if (currentItemIndex != -1)
                    sendPlayState()
            }

            AVPlayerItemStatusFailed -> {
                Log("checkItemStatus -> status= $status - Failed")
                scope.launch { player?.removeAllItems() }
                sendErrorState()
            }

            else -> { // handle unknown/buffering states
                Log("checkItemStatus -> status= $status - Unknown")
                sendState(PlayerState.Loading)
            }
        }
    }

    private fun sendState(state: PlayerState) {
        Log("sendState -> state = $state - idx = $currentItemIndex")
        playerState.trySend(currentItemIndex to state)
        MediaCenterManager.bindCenterInfo(state, playerItems.getOrNull(currentItemIndex)?.title)
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
            if (selectedIndex != currentItemIndex ||
                playerItems[selectedIndex].id != player?.currentItem?.toPlayerItem()?.id
            )
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
        player?.setActionAtItemEnd(AVPlayerActionAtItemEndNone)
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
            if (player?.items()?.isEmpty() ?: true) {
                if (currentItemIndex < playerItems.lastIndex)
                    play(currentItemIndex + 1)
            } else {
                player?.advanceToNextItem()
            }
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