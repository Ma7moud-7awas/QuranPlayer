package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class PlayerRepoImpl(
    private val playerSource: PlayerSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : PlayerRepo {

    private companion object {
        // ex: https://server7.mp3quran.net/s_gmd/001.mp3
        private const val BASE_URL = "https://server7.mp3quran.net/s_gmd/"
        private const val EXTENSION = ".mp3"

        fun mapIdToUrl(id: String): String {
            return BASE_URL + id + EXTENSION
        }
    }

    override val playerState: Flow<PlayerState> =
        playerSource.playerState.receiveAsFlow()

    override suspend fun setItem(id: String) {
        withContext(dispatcher) {
            playerSource.setItem(mapIdToUrl(id))
        }
    }

    override suspend fun play(id: String) {
        withContext(dispatcher) {
            playerSource.play(mapIdToUrl(id))
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