package com.m7.quranplayer.player.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PlayerAction {

    data object Play : PlayerAction

    data object Pause : PlayerAction

    data object Next : PlayerAction

    data object Previous : PlayerAction

    data object Repeat : PlayerAction

    @Immutable
    data class SeekTo(val positionMs: Long) : PlayerAction
}