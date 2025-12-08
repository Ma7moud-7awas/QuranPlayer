package com.m7.mediaplayer.chapter.data

import com.m7.mediaplayer.chapter.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface PlayerSource {

    val playerState: Channel<PlayerState>

    fun setItem(url: String)

    fun play(url: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun release()
}