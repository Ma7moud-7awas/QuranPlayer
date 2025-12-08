package com.m7.mediaplayer.core.di

import com.m7.mediaplayer.chapter.data.ChaptersRepoImpl
import com.m7.mediaplayer.chapter.data.PlayerRepoImpl
import com.m7.mediaplayer.chapter.domain.repo.ChapterRepo
import com.m7.mediaplayer.chapter.domain.repo.PlayerRepo
import com.m7.mediaplayer.chapter.ui.ChapterViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun initKoin(appDeclaration: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        appDeclaration?.invoke(this)

        modules(
            platformModule,
            module {
                viewModelOf(::ChapterViewModel)

                single<ChapterRepo> { ChaptersRepoImpl() }
                single<PlayerRepo> { PlayerRepoImpl(get()) }
            }
        )
    }
}
