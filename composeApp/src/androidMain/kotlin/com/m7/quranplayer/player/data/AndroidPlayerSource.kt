package com.m7.quranplayer.player.data

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.m7.quranplayer.core.Log
import com.m7.quranplayer.player.PlayerService
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class AndroidPlayerSource(private val context: Context) : PlayerSource {

    // handles service communication & session lifecycle
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var player: MediaController? = null

    override val playerState: Channel<PlayerState> = Channel(CONFLATED)

    private var currentItemUrl: String? = null
    private var isCurrentItemEnded = false
    private var playerError: Exception? = null

    init {
        initController()
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
                            playerState.trySend(PlayerState.Idle)
                        }

                        Player.STATE_BUFFERING -> {
                            playerState.trySend(PlayerState.Loading)
                        }

                        Player.STATE_READY -> getPlayer().play()

                        Player.STATE_ENDED -> {
                            isCurrentItemEnded = true
                            playerState.trySend(PlayerState.Ended)
                        }
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log("PlayerSource -> isPlaying= $isPlaying")
                if (isPlaying) {
                    isCurrentItemEnded = false
                    playerState.trySend(
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
                        ))
                } else if (!isCurrentItemEnded) {
                    playerState.trySend(PlayerState.Paused)
                }
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log("PlayerSource -> error= $error")
                playerError = error

                error?.also {
                    playerState.trySend(PlayerState.Error(error))
                }
            }
        })
    }

    override fun setItem(url: String) {
        currentItemUrl = url
        getPlayer().setMediaItem(MediaItem.fromUri(url))
    }

    override fun play(url: String) {
        getPlayer().apply {
            playerError?.also {
                // reset the player if there is an error to start over
                prepare()
            }

            if (currentItemUrl != url || currentMediaItem == null) {
                // start new media item
                setItem(url)
            } else if (currentItemUrl == url && isCurrentItemEnded) {
                // replay the current media item
                seekTo(0)
            }

            play()
        }
    }

    override fun pause() {
        getPlayer().pause()
    }

    override fun seekTo(positionMs: Long) {
        getPlayer().seekTo(positionMs)
    }

    override fun repeat() {
        getPlayer().apply {
            seekTo(0)
            play()
        }
    }

    override fun release() {
        getPlayer().release()
        player = null
        playerError = null
        currentItemUrl = null
    }
}