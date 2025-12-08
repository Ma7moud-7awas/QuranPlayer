package com.m7.mediaplayer.chapter.domain.model

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

@Stable
sealed interface PlayerState {

    data object Idle : PlayerState

    data object Loading : PlayerState

    data class Playing(
        val duration: Long,
        val updatedPosition: Flow<Long>
    ) : PlayerState

    data object Paused : PlayerState

    data object Ended : PlayerState

    data class Error(val error: Exception?) : PlayerState
}