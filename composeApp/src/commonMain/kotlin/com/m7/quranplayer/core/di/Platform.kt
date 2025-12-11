package com.m7.quranplayer.core.di

import org.koin.core.module.Module

expect val platformModule: Module

expect fun Int.format(format: String): String

expect fun Int.localize(format: String = "%d"): String