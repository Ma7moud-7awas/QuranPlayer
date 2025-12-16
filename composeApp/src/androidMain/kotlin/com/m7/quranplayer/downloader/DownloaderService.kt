package com.m7.quranplayer.downloader

import android.app.Notification
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import com.m7.quranplayer.R
import org.koin.android.ext.android.inject

const val FOREGROUND_NOTIFICATION_ID = 1
const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
const val WORK_NAME = "download_work"

@UnstableApi
class DownloaderService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_channel_name,
    R.string.download_channel_desc,
) {
    private val downloadUtil: DownloadUtil by inject()

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = downloadUtil.getDownloadManager()

        val context = applicationContext
        val downloadNotificationHelper = downloadUtil.getDownloadNotificationHelper(this)
        var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    val notification: Notification? = when (download.state) {
                        Download.STATE_COMPLETED -> {
                            downloadNotificationHelper.buildDownloadCompletedNotification(
                                context,
                                // todo: replace with success icon
                                R.drawable.ic_launcher_foreground,
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

    override fun getScheduler(): Scheduler? {
        return WorkManagerScheduler(this, WORK_NAME)
    }

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        return downloadUtil.getDownloadNotificationHelper(this).buildProgressNotification(
            this,
            // todo: replace with download icon
            R.drawable.ic_launcher_foreground,
            null, null,
            downloads,
            notMetRequirements
        )
    }
}