package com.m7.mediaplayer.chapter.domain.repo

import com.m7.mediaplayer.chapter.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepo {

    val playerState: Flow<PlayerState>

    fun setItem(id: String)

    fun play(id: String)

    fun pause()

    fun seekTo(positionMs: Long)

    fun release()
}