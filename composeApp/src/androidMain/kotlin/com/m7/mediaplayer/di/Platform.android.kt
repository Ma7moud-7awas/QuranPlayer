package com.m7.mediaplayer.di

import com.m7.mediaplayer.AndroidPlayer
import com.m7.mediaplayer.Player
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<Player> { AndroidPlayer(androidContext()) }
}