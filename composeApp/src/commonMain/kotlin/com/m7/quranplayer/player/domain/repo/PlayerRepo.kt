package com.m7.quranplayer.player.domain.repo

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepo {

    val playerState: Flow<Pair<Int, PlayerState>>

    val playerAction: Flow<PlayerAction>

    suspend fun setPlaylist(items: List<Chapter>)

    suspend fun play(selectedIndex: Int)

    suspend fun previous()

    suspend fun next()

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun enableRepeat(enable: Boolean)

    suspend fun release()
}