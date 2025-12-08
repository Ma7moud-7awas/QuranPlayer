package com.m7.mediaplayer.core.di

import com.m7.mediaplayer.chapter.data.AndroidPlayerSource
import com.m7.mediaplayer.chapter.data.PlayerSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.Locale

actual val platformModule = module {
    single<PlayerSource> { AndroidPlayerSource(androidContext()) }
}

actual fun Number.format(format: String) = String.format(Locale.ROOT, format, this)

actual fun Number.formatLocalized(format: String) = String.format(format, this)