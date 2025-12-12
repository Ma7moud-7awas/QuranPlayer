package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel

interface PlayerSource {

    val playerState: Channel<PlayerState>

    fun setItem(url: String)

    fun play(url: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun repeat()

    fun release()
}