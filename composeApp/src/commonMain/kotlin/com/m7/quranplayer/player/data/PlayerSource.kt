package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel

interface PlayerSource {

    val playerState: Channel<Pair<String?, PlayerState>>

    val playerAction: Channel<PlayerAction>

    suspend fun play(items: List<PlayerItem>)

    suspend fun previous(items: List<PlayerItem>)

    suspend fun next()

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun repeat()

    suspend fun release()
}