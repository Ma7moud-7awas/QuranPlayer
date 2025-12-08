package com.m7.mediaplayer.chapter.data

import platform.UIKit.UIDevice

class IOSPlayerSource: PlayerSource {
    private val player = AVPlayer().init()

    override val isPlaying: Boolean
        get() = player.timeControlStatus.playing

    override val duration: Long
        get() = player.currentItem.duration() ?: 0

    override val currentPosition: Long
        get() = player.currentPosition

    override fun addItem(url: String) {
        player.replaceCurrentItem(AVPlayerItem(url))
    }

    override fun play(url: String) {
        if (player.currentItem == null) {
            addItem(url)
        }

        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seek(positionMs)
    }

    override fun release() {
        player.release()
    }
}