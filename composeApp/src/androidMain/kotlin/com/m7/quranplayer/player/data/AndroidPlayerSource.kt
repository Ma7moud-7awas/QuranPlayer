package com.m7.quranplayer.player.data

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.data.Url
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.saad_al_ghamdy
import kotlin.math.max

class AndroidPlayerSource(
    context: Context,
    private val playerScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
) : PlayerSource {

    // handles service communication & session lifecycle
    val sessionToken = SessionToken(
        context, ComponentName(context, PlayerService::class.java)
    )
    private val mediaFuture = MediaController.Builder(context, sessionToken).buildAsync()
    private val player: MediaController by lazy { mediaFuture.get() }

    override val playerState: Channel<Pair<Int, PlayerState>> = Channel(CONFLATED)

    override val playerAction: Channel<PlayerAction> = Channel(CONFLATED)

    private var currentState: PlayerState = PlayerState.Idle
    private var playerError: Exception? = null
    private var repeatMode = RepeatMode.None

    init {
        mediaFuture.addListener(
            { observePlayerUpdates() }, MoreExecutors.directExecutor()
        )
    }

    private fun Player.launch(scope: CoroutineScope = playerScope, block: Player.() -> Unit) {
        scope.launch { block() }
    }

    private fun observePlayerUpdates() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                playerError ?: run {
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            Log("onPlaybackStateChanged -> playbackState= $playbackState - IDLE")
                            sendState(PlayerState.Idle)
                        }

                        Player.STATE_BUFFERING -> {
                            Log("onPlaybackStateChanged -> playbackState= $playbackState - BUFFERING")
                            sendState(PlayerState.Loading)
                        }

                        Player.STATE_READY -> {
                            Log("onPlaybackStateChanged -> playbackState= $playbackState - READY")
                            player.play()
                        }

                        Player.STATE_ENDED -> {
                            Log("onPlaybackStateChanged -> playbackState= $playbackState - ENDED")
                            // repeat last item
                            if (repeatMode == RepeatMode.One)
                                player.seekTo(0)
                            else
                                sendState(PlayerState.Ended)
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log("onIsPlayingChanged -> isPlaying= $isPlaying")
                playerError ?: run {
                    if (isPlaying) {
                        sendPlayState()
                    } else if (currentState !is PlayerState.Ended && currentState !is PlayerState.Loading) {
                        sendState(PlayerState.Paused)
                    }
                }
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log("onPlayerErrorChanged -> error= $error")
                playerError = error

                error?.also {
                    sendState(PlayerState.Error(error))
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                Log("onMediaItemTransition -> reason = $reason")
                mediaItem?.toPlayerItem()?.also {
                    Log("onMediaItemTransition -> newItem id = ${it.id}")
                    if (reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        player.stop()
                        if (repeatMode == RepeatMode.One) {
                            player.seekToPreviousMediaItem()
                        }
                        player.play()
                    }
                }
            }
        })
    }

    private fun sendState(state: PlayerState) {
        Log("sendState -> state = $state - idx = ${player.currentMediaItemIndex}")
        currentState = state
        playerState.trySend(player.currentMediaItemIndex to state)
    }

    private fun sendPlayState() {
        sendState(
            PlayerState.Playing(
                duration = max(0, player.duration),
                updatedPosition = flow {
                    player.apply {
                        while (currentPosition <= duration) {
                            delay(200)
                            emit(currentPosition)
                        }
                    }
                }
            )
        )
    }

    override suspend fun setPlaylist(items: List<PlayerItem>) {
        val items = items.map {
            it.toMediaItem(getString(Res.string.saad_al_ghamdy))
        }

        player.launch {
            setMediaItems(items)
            stop()
        }
    }

    override suspend fun play(selectedIndex: Int) {
        player.launch {
            playerError?.also {// if there is an error,
                // reset the player to start over.
                prepare()
            }

            if (selectedIndex != currentMediaItemIndex) {
                seekTo(selectedIndex, 0)
            }

            play()
        }
    }

    override suspend fun pause() {
        player.launch { pause() }
    }

    override suspend fun previous() {
        player.launch {
            seekToPreviousMediaItem()
            playerError?.also { prepare() }
        }
    }

    override suspend fun next() {
        player.launch {
            seekToNextMediaItem()
            playerError?.also { prepare() }
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        player.launch { seekTo(positionMs) }
    }

    override suspend fun enableRepeat(enable: Boolean) {
        repeatMode = if (enable) RepeatMode.One else RepeatMode.None
    }

    override suspend fun release() {
        player.launch { release() }
        playerError = null
    }
}

fun PlayerItem.toMediaItem(reciter: String): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(Url.getDownloadUrlById(id))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setArtist(reciter)
                .setTitle(title)
                .build()
        )
        .build()

fun MediaItem.toPlayerItem(): PlayerItem =
    PlayerItem(
        id = mediaId,
        title = mediaMetadata.title.toString()
    )