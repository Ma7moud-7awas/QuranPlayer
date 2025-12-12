package com.m7.quranplayer.player.data

import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class PlayerRepoImpl(
    private val playerSource: PlayerSource
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

    override fun setItem(id: String) {
        playerSource.setItem(mapIdToUrl(id))
    }

    override fun play(id: String) {
        playerSource.play(mapIdToUrl(id))
    }

    override fun pause() {
        playerSource.pause()
    }

    override fun seekTo(positionMs: Long) {
        playerSource.seekTo(positionMs)
    }

    override fun repeat() {
        playerSource.repeat()
    }

    override fun release() {
        playerSource.release()
    }
}