package com.m7.quranplayer.player

import android.app.Notification
import android.content.Context
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
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
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import com.m7.quranplayer.R
import java.io.File
import java.util.concurrent.Executors

const val FOREGROUND_NOTIFICATION_ID = 1
const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
const val WORK_NAME = "download_work"

@UnstableApi
class PlayerDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
//    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
//    R.string.download_channel_name,
//    R.string.download_channel_desc,
) {

    private var downloadManager: DownloadManager? = null
    private var downloadCache: Cache? = null
    private var downloadDirectory: File? = null
    private var databaseProvider: DatabaseProvider? = null
    private var httpDataSourceFactory: DataSource.Factory? = null
    private var notificationHelper: DownloadNotificationHelper? = null

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = getDownloadManager(this)

        val context = applicationContext
        val downloadNotificationHelper = getDownloadNotificationHelper()
        var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
//                    super.onDownloadChanged(downloadManager, download, finalException)

                    val notification: Notification? = when (download.state) {
                        Download.STATE_COMPLETED -> {
                            downloadNotificationHelper.buildDownloadCompletedNotification(
                                context,
                                // todo: replace with success icon
                                R.drawable.icon_512x512,
                                /* contentIntent= */ null,
                                Util.fromUtf8Bytes(download.request.data)
                            );
                        }

                        Download.STATE_FAILED -> {
                            downloadNotificationHelper.buildDownloadFailedNotification(
                                context,
                                // todo: replace with failure icon
                                R.drawable.icon_512x512,
                                /* contentIntent= */ null,
                                Util.fromUtf8Bytes(download.request.data)
                            )
                        }

                        else -> {
                            return
                        }
                    }
                    NotificationUtil.setNotification(context, nextNotificationId++, notification)
                }
            }
        )

        return downloadManager
    }

    private fun getDownloadManager(context: Context): DownloadManager {
        return downloadManager ?: run {
            DownloadManager(
                context,
                getDatabaseProvider(),
                getDownloadCache(this),
                getDataSourceFactory(),
                Executors.newFixedThreadPool(3)
            ).also {
                downloadManager = it
            }
//            downloadTracker =
//                DownloadTracker(context, getHttpDataSourceFactory(context), downloadManager)
        }
    }

    private fun getDatabaseProvider(): DatabaseProvider {
        return databaseProvider ?: run {
            StandaloneDatabaseProvider(this).also {
                databaseProvider = it
            }
        }
    }

    @Synchronized
    private fun getDownloadCache(context: Context): Cache {
        return downloadCache ?: run {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider()
            ).also {
                downloadCache = it
            }
        }
    }

    private fun getDownloadDirectory(context: Context): File {
        return downloadDirectory ?: run {
            context.getExternalFilesDir( /* type= */null)
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

    private fun getDataSourceFactory(): DataSource.Factory {
        return httpDataSourceFactory ?: run {
            DefaultDataSource.Factory(
                applicationContext,
                DefaultHttpDataSource.Factory()
            ).let {
                buildReadOnlyCacheDataSource(it, getDownloadCache(applicationContext))
                    .also {
                        httpDataSourceFactory = it
                    }
            }
        }
    }

    private fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        return notificationHelper ?: run {
            DownloadNotificationHelper(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
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

    override fun getScheduler(): Scheduler? {
        return WorkManagerScheduler(this, WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return getDownloadNotificationHelper().buildProgressNotification(
            this,
            // todo: replace with download icon
            R.drawable.icon_512x512,
            null, null,
            downloads,
            notMetRequirements
        )
    }
}