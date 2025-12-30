package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class PlayerRepoImpl(
    private val playerSource: PlayerSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : PlayerRepo {

    override val playerState: Flow<PlayerState> =
        playerSource.playerState.receiveAsFlow()

    override suspend fun play(id: String, title: String) {
        withContext(dispatcher) {
            playerSource.play(id, title)
        }
    }

    override suspend fun pause() {
        withContext(dispatcher) {
            playerSource.pause()
        }
    }

    override suspend fun seekTo(positionMs: Long) {
        withContext(dispatcher) {
            playerSource.seekTo(positionMs)
        }
    }

    override suspend fun repeat() {
        withContext(dispatcher) {
            playerSource.repeat()
        }
    }

    override suspend fun release() {
        withContext(dispatcher) {
            playerSource.release()
        }
    }
}