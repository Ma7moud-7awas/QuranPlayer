package com.m7.quranplayer.player.domain.repo

import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepo {

    val playerState: Flow<PlayerState>

    fun setItem(id: String)

    fun play(id: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun repeat()

    fun release()
}