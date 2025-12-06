package com.m7.mediaplayer

interface Player {
    val player: Any

    fun play(url: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun release()
}