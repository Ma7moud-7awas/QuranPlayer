package com.m7.mediaplayer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AndroidPlayer(context: Context) : Player {
    override val player = ExoPlayer.Builder(context).build()
        .apply {
            prepare()
        }

    override val isPlaying: Boolean
        get() = player.isPlaying

    override val duration: Long
        get() = player.duration

    override val currentPosition: Long
        get() = player.currentPosition

    override fun addItem(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
    }

    override fun play(url: String) {
        if (player.currentMediaItem == null) {
            addItem(url)
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
    }
}