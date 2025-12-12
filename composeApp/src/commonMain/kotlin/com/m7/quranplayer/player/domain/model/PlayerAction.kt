package com.m7.quranplayer.player.domain.model

import androidx.compose.runtime.Stable

@Stable
sealed interface PlayerAction {

    data object Play : PlayerAction

    data object Pause : PlayerAction

    data object Next : PlayerAction

    data object Previous : PlayerAction

    data object Repeat : PlayerAction

    data class SeekTo(val positionMs: Long) : PlayerAction
}