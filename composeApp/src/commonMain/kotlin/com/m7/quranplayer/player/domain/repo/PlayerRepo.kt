package com.m7.quranplayer.player.domain.repo

import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepo {

    val playerState: Flow<PlayerState>

    suspend fun play(id: String, title: String)

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun repeat()

    suspend fun release()
}