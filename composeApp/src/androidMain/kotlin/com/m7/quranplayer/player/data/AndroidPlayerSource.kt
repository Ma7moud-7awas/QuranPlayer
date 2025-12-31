package com.m7.quranplayer.player.data

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.core.data.Url
import com.m7.quranplayer.player.domain.model.PlayerState
import com.m7.quranplayer.player.domain.model.PlayerAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.saad_al_ghamdy

class AndroidPlayerSource(private val context: Context) : PlayerSource {

    // handles service communication & session lifecycle
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var player: MediaController? = null

    override val playerState: Channel<Pair<String?, PlayerState>> = Channel(CONFLATED)

    override val playerAction: Channel<PlayerAction> = Channel(CONFLATED)

    private var currentItem: PlayerItem? = null
    private var isCurrentItemEnded = false
    private var playerError: Exception? = null

    init {
        initController()

        PlayerProvider.initForwardingPlayer(
            context,
            onPrevious = {
                playerAction.trySend(PlayerAction.Previous)
            },
            onNext = {
                playerAction.trySend(PlayerAction.Next)
            }
        )
    }


    private fun sendState(state: PlayerState) {
        playerState.trySend(currentItem?.id to state)
    }

    private fun initController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()
                observePlayerUpdates()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun getPlayer(): MediaController {
        return player ?: controllerFuture.get()
            .also {
                player = it
            }
    }

    private fun observePlayerUpdates() {
        getPlayer().prepare()
        getPlayer().addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log("PlayerSource -> playbackState= $playbackState")
                playerError ?: run {
                    when (playbackState) {
                        Player.STATE_IDLE -> {
                            sendState(PlayerState.Idle)
                        }

                        Player.STATE_BUFFERING -> {
                            sendState(PlayerState.Loading)
                        }

                        Player.STATE_READY -> getPlayer().play()

                        Player.STATE_ENDED -> {
                            isCurrentItemEnded = true
                            sendState(PlayerState.Ended)
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log("PlayerSource -> isPlaying= $isPlaying")
                if (isPlaying) {
                    isCurrentItemEnded = false
                    sendState(
                        PlayerState.Playing(
                            duration = getPlayer().duration,
                            updatedPosition = flow {
                                getPlayer().apply {
                                    while (currentPosition <= duration) {
                                        delay(200)
                                        emit(currentPosition)
                                    }
                                }
                            }
                        )
                    )
                } else if (!isCurrentItemEnded) {
                    sendState(PlayerState.Paused)
                }
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log("PlayerSource -> error= $error")
                playerError = error

                error?.also {
                    sendState(PlayerState.Error(error))
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                getPlayer().also {
                    it.stop()
                    it.playWhenReady = false
                    it.removeMediaItems(0, it.mediaItemCount)
                }
            }
        })
    }

    suspend fun setItems(items: List<PlayerItem>) {
        val items = items.map {
            MediaItem.Builder()
                .setMediaId(it.id)
                .setUri(Url.getDownloadUrlById(it.id))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(getString(Res.string.saad_al_ghamdy))
                        .setTitle(it.title)
                        .build()
                )
                .build()
        }
        getPlayer().setMediaItems(items)
    }

    override suspend fun play(items: List<PlayerItem>) {
        val firstItem = items.firstOrNull() ?: return

        getPlayer().apply {
            playerError?.also {
                // reset the player if there is an error to start over
                prepare()
            }

            if (currentItem?.id != firstItem.id || currentMediaItem == null) {
                // start new media item
                setItems(items)
                currentItem = firstItem
            } else if (currentItem?.id == firstItem.id && isCurrentItemEnded) {
                // replay the current media item
                seekTo(0)
            }

            play()
        }
    }

    override suspend fun pause() {
        getPlayer().pause()
    }

    override suspend fun previous(items: List<PlayerItem>) {
        getPlayer().seekToPreviousMediaItem()
    }

    override suspend fun next() {
        getPlayer().seekToNextMediaItem()
    }


    override suspend fun seekTo(positionMs: Long) {
        getPlayer().seekTo(positionMs)
    }

    override suspend fun repeat() {
        getPlayer().apply {
            seekTo(0)
            play()
        }
    }

    override suspend fun release() {
        getPlayer().release()
        player = null
        playerError = null
        currentItem = null
    }
}