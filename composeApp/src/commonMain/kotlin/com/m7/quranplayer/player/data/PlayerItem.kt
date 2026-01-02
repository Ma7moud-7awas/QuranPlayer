package com.m7.quranplayer.player.data

import com.m7.quranplayer.chapter.domain.model.Chapter

data class PlayerItem(
    val id: String,
    val title: String,
)

fun List<Chapter>.toPlayerItems() =
    map { (id, _, title, _) ->
        PlayerItem(id, title)
    }