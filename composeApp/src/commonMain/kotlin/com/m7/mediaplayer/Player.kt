package com.m7.mediaplayer

interface Player {
    val player: Any

    val isPlaying: Boolean

    val duration: Long

    val currentPosition: Long

    fun addItem(url: String)

    fun play(url: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun release()
}