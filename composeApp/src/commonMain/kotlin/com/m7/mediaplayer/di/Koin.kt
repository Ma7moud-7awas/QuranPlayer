package com.m7.mediaplayer.di

import com.m7.mediaplayer.ChapterViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun initKoin(appDeclaration: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        appDeclaration

        modules(
            platformModule,
            module {
                viewModelOf(::ChapterViewModel)
            }
        )
    }
}
