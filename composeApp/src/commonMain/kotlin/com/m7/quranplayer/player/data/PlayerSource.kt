package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel

interface PlayerSource {

    val playerState: Channel<PlayerState>

    suspend fun play(id: String, title: String)

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun repeat()

    suspend fun release()
}