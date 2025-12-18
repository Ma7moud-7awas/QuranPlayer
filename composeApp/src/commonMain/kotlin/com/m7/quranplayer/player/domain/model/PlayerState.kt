package com.m7.quranplayer.player.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

@Immutable
sealed interface PlayerState {

    data object Idle : PlayerState

    data object Loading : PlayerState

    data object Paused : PlayerState

    data object Ended : PlayerState

    @Immutable
    data class Playing(val duration: Long, val updatedPosition: Flow<Long>) : PlayerState

    @Immutable
    data class Error(val error: Exception?) : PlayerState
}