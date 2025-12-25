package com.m7.quranplayer.player.data

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.m7.quranplayer.downloader.data.DownloadUtil

class ExoPlayerProvider(private val context: Context, private val downloadUtil: DownloadUtil) {

    fun provide(): ExoPlayer {
        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory()
                .setCache(downloadUtil.getDownloadCache(context))
                .setUpstreamDataSourceFactory(downloadUtil.getHttpDataSourceFactory())
                .setCacheWriteDataSinkFactory(null) // Disable writing.

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(cacheDataSourceFactory)
            )
            .build()
    }
}