package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.channels.Channel

interface PlayerSource {

    val playerState: Channel<Pair<Int, PlayerState>>

    /** route media center actions */
    val playerAction: Channel<PlayerAction>

    suspend fun setPlaylist(items: List<PlayerItem>)

    suspend fun play(selectedIndex: Int)

    suspend fun previous()

    suspend fun next()

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun enableRepeat(enable: Boolean)

    suspend fun release()
}