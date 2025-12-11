package com.m7.quranplayer.core.di

import com.m7.quranplayer.chapter.data.ChaptersRepoImpl
import com.m7.quranplayer.player.data.PlayerRepoImpl
import com.m7.quranplayer.chapter.domain.repo.ChapterRepo
import com.m7.quranplayer.player.domain.repo.PlayerRepo
import com.m7.quranplayer.chapter.ui.ChapterViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

fun initKoin(appDeclaration: (KoinApplication.() -> Unit)? = null) {
    if (KoinPlatformTools.defaultContext().getOrNull() == null)
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
