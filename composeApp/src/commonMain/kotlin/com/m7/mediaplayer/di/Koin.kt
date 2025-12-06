package com.m7.mediaplayer.di

import com.m7.mediaplayer.ChapterViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun initKoin(platform: Platform) {
    startKoin {
        modules(
            platform.module,
            module {
                factoryOf(::ChapterViewModel)
            }
        )
    }
}
