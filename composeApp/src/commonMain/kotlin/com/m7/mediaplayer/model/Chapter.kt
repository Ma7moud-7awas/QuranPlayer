package com.m7.mediaplayer.model

data class Chapter(
    val id: Int,
    val title: String,
    val durationSeconds: Int,
    val url: String,
)