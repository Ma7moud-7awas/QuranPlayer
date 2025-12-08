package com.m7.mediaplayer.chapter.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.m7.mediaplayer.chapter.domain.model.PlayerState
import com.m7.mediaplayer.core.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class AndroidPlayerSource(
    context: Context,
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
) : PlayerSource {

    override val playerState: Channel<PlayerState> = Channel(CONFLATED)
    var currentItemUrl: String? = null
    var isCurrentItemEnded = false
    var playerError: Exception? = null

    init {
        player.prepare()
        player.addListener(object : Player.Listener {
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

                        Player.STATE_READY -> player.play()


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
                            duration = player.duration,
                            updatedPosition = flow {
                                with(player) {
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
        player.setMediaItem(MediaItem.fromUri(url))
        currentItemUrl = url
    }

    override fun play(url: String) {
        playerError?.also {
            // reset the player if there is an error to start over
            player.prepare()
        }

        if (currentItemUrl != url || player.currentMediaItem == null) {
            // start new media item
            setItem(url)
        } else if (currentItemUrl == url && isCurrentItemEnded) {
            // replay the current media item
            player.seekTo(0)
        }

        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    override fun release() {
        player.release()
        currentItemUrl = null
    }
}