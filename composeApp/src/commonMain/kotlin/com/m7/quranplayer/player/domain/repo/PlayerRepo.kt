package com.m7.quranplayer.player.domain.repo

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.player.data.PlayerItem
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepo {

    val playerState: Flow<Pair<String?, PlayerState>>

    val playerAction: Flow<PlayerAction>

    suspend fun play(items: List<Chapter>)

    suspend fun previous(items: List<Chapter>)

    suspend fun next()

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun repeat()

    suspend fun release()
}