package com.m7.quranplayer.player.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.m7.quranplayer.downloader.data.DownloadUtil
import org.koin.java.KoinJavaComponent.inject

@OptIn(UnstableApi::class)
object PlayerProvider {

    private val downloadUtil: DownloadUtil by inject(DownloadUtil::class.java)

    private var exoPlayer: ExoPlayer? = null

    fun getPlayer(context: Context): ExoPlayer {
        return exoPlayer ?: run {
            val cacheDataSourceFactory: DataSource.Factory =
                CacheDataSource.Factory()
                    .setCache(downloadUtil.getDownloadCache(context))
                    .setUpstreamDataSourceFactory(downloadUtil.getHttpDataSourceFactory())
                    .setCacheWriteDataSinkFactory(null) // Disable writing.

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()

            return ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(context)
                        .setDataSourceFactory(cacheDataSourceFactory)
                )
                .setAudioAttributes(audioAttributes, true)
                .build()
                .also {
                    exoPlayer = it
                }
        }
    }
}