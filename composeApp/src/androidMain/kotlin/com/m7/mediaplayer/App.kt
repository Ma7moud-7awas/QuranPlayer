package com.m7.mediaplayer

import android.app.Application
import com.m7.mediaplayer.di.Platform
import com.m7.mediaplayer.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class App : Application(), Platform {

    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }

    override val module = module {
        single { getPlayer(androidContext()) }
    }
}
