package com.m7.mediaplayer

import platform.UIKit.UIDevice

class IOSPlayer: Player {
    override val player = IOSPlayer()

    override fun play(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
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

actual fun getPlayer(): Player = IOSPlayer()