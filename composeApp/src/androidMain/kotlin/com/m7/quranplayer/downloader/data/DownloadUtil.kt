package com.m7.quranplayer.downloader.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import com.m7.quranplayer.downloader.DOWNLOAD_CONTENT_DIRECTORY
import com.m7.quranplayer.downloader.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import java.io.File
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
class DownloadUtil(private val context: Context) {

    private var downloadManager: DownloadManager? = null
    private var downloadCache: Cache? = null
    private var downloadDirectory: File? = null
    private var databaseProvider: DatabaseProvider? = null
    private var httpDataSourceFactory: DataSource.Factory? = null
    private var notificationHelper: DownloadNotificationHelper? = null

    fun getDownloadManager(): DownloadManager {
        return downloadManager ?: run {
            DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                getHttpDataSourceFactory(),
                Executors.newFixedThreadPool(3)
            ).also {
                downloadManager = it
            }
        }
    }

    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        return databaseProvider ?: run {
            StandaloneDatabaseProvider(context).also {
                databaseProvider = it
            }
        }
    }

    fun getDownloadCache(context: Context): Cache {
        return downloadCache ?: run {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            ).also {
                downloadCache = it
            }
        }
    }

    private fun getDownloadDirectory(context: Context): File {
        return downloadDirectory ?: run {
            context.getExternalFilesDir(null)
                ?.also {
                    downloadDirectory = it
                }
                ?: run {
                    context.getFilesDir().also {
                        downloadDirectory = it
                    }
                }
        }
    }

    fun getHttpDataSourceFactory(): DataSource.Factory {
        return httpDataSourceFactory ?: run {
            DefaultDataSource.Factory(
                context.applicationContext,
                DefaultHttpDataSource.Factory()
            ).let {
                buildReadOnlyCacheDataSource(it, getDownloadCache(context.applicationContext))
                    .also {
                        httpDataSourceFactory = it
                    }
            }
        }
    }

    fun getDownloadNotificationHelper(context: Context): DownloadNotificationHelper {
        return notificationHelper ?: run {
            DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
                .also {
                    notificationHelper = it
                }
        }
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory?, cache: Cache
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getDownload(id: String): Download? {
        return getDownloadManager().downloadIndex.getDownload(id)
    }
}