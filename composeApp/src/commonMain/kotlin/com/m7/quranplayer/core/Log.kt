package com.m7.quranplayer.core

object Log {

    const val TAG = "|M| - "

    operator fun invoke(text: String?, tag: String = TAG) {
        println(tag + text)
    }

    operator fun invoke(text: Any?, tag: String = TAG) {
        println(tag + text)
    }

    fun Any.log() {
        invoke(toString())
    }
}