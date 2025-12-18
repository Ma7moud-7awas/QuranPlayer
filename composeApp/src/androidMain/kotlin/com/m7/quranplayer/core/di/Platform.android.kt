package com.m7.quranplayer.core.di

import androidx.media3.exoplayer.ExoPlayer
import com.m7.quranplayer.downloader.DownloadUtil
import com.m7.quranplayer.downloader.data.AndroidDownloaderSource
import com.m7.quranplayer.downloader.data.DownloaderSource
import com.m7.quranplayer.player.data.AndroidPlayerSource
import com.m7.quranplayer.player.data.ExoPlayerProvider
import com.m7.quranplayer.player.data.PlayerSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.Locale

actual val platformModule = module {

    single { DownloadUtil(androidContext()) }

    single<ExoPlayer> {
        ExoPlayerProvider(androidContext(), get()).provide()
    }

    single<PlayerSource> { AndroidPlayerSource(androidContext()) }

    single<DownloaderSource> { AndroidDownloaderSource(androidContext(), get()) }
}

actual fun Int.format(format: String) = String.format(Locale.ROOT, format, this)

actual fun Int.localize(format: String): String = String.format(format, this)