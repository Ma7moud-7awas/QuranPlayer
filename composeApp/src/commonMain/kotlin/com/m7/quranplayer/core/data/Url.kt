package com.m7.quranplayer.core.data

object Url {
    // ex: https://server7.mp3quran.net/s_gmd/001.mp3
    private const val BASE_URL = "https://server7.mp3quran.net/s_gmd/"
    private const val EXTENSION = ".mp3"

    fun getDownloadUrlById(id: String): String {
        return BASE_URL + id + EXTENSION
    }
}