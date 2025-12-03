package com.m7.mediaplayer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform