package com.m7.mediaplayer.core.di

import android.system.Os.bind
import com.m7.mediaplayer.AndroidPlayer
import com.m7.mediaplayer.Player
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    single<PlayerSource> { IOSPlayerSource() }
}

actual fun Number.format(format: String) = NSString.stringWithFormat(format, this)

actual fun Number.formatLocalized(format: String) = format(format, this)