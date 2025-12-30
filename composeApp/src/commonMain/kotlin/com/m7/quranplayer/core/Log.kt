package com.m7.quranplayer.core

object Log {

    const val TAG = "|M|"

    operator fun invoke(text: Any?, prefix: String = "", tag: String = TAG) {
        println("$tag - $prefix $text")
    }

    operator fun invoke(text: String?, prefix: String) {
        invoke(text, prefix, TAG)
    }

    fun <T> T.log(prefix: String = ""): T {
        invoke(toString(), prefix)
        return this
    }
}