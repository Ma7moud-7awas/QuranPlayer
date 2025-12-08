package com.m7.mediaplayer.core

object Log {

    const val TAG = "|M|"

    operator fun invoke(text: String?, tag: String = TAG) {
        println(tag + text)
    }

    operator fun invoke(text: Any?, tag: String = TAG) {
        println(tag + text)
    }
}