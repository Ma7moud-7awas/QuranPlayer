package com.m7.quranplayer.player.data

import com.m7.quranplayer.chapter.domain.model.Chapter
import com.m7.quranplayer.player.domain.model.PlayerAction
import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.player.domain.model.PlayerState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

class PlayerRepoImpl(
    private val playerSource: PlayerSource
) : PlayerRepo {

    override val playerState: Flow<Pair<Int, PlayerState>> =
        playerSource.playerState.receiveAsFlow()

    override val playerAction: Flow<PlayerAction> =
        playerSource.playerAction.receiveAsFlow()

    override suspend fun setPlaylist(items: List<Chapter>) {
        playerSource.setPlaylist(items.toPlayerItems())
    }

    override suspend fun play(selectedIndex: Int) {
        playerSource.play(selectedIndex)
    }

    override suspend fun previous() {
        playerSource.previous()
    }

    override suspend fun next() {
        playerSource.next()
    }

    override suspend fun pause() {
        playerSource.pause()
    }

    override suspend fun seekTo(positionMs: Long) {
        playerSource.seekTo(positionMs)
    }

    override suspend fun enableRepeat(enable: Boolean) {
        playerSource.enableRepeat(enable)
    }

    override suspend fun release() {
        playerSource.release()
    }
}