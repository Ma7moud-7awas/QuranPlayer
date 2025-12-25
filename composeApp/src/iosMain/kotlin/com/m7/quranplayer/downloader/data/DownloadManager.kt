package com.m7.quranplayer.downloader.data

import com.m7.quranplayer.core.data.Url
import com.m7.quranplayer.downloader.domain.model.DownloadState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UniformTypeIdentifiers.URLByAppendingPathComponent
import platform.UniformTypeIdentifiers.UTTypeAudio
import platform.UniformTypeIdentifiers.UTTypeDirectory

@OptIn(ExperimentalForeignApi::class)
object DownloadManager {

    private val fileManager = NSFileManager.defaultManager()
    private val directoryUrl =
        fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            .first() as NSURL

    fun getDownloadUrl(id: String): String {
        return getCompletedLocalUrl(id).let { localUrl ->
            if (checkDownloadExists(id, localUrl))
                localUrl.toString() else Url.getDownloadUrlById(id)
        }
    }

    fun getCompletedLocalUrl(id: String): NSURL {
        return directoryUrl.URLByAppendingPathComponent(
            "$id.mp3",
            UTTypeAudio
        )
    }

    fun checkDownloadExists(id: String, url: NSURL = getCompletedLocalUrl(id)): Boolean {
        return url.path?.let {
            fileManager.fileExistsAtPath(it)
        } ?: false
    }

    fun moveDownload(id: String, sourceUrl: NSURL): Boolean {
        // remove pause directory
        removeDownload(id, getPausedDownloadDirectoryUrl(id))

        getCompletedLocalUrl(id).let { localUrl ->
            removeDownload(id, localUrl)

            return fileManager.moveItemAtURL(
                sourceUrl, localUrl, null
            )
        }
    }

    fun saveDownloadData(id: String, data: NSData, progress: Float): Boolean {
        // remove old pause directory and it's children
        removeDownload(id, getPausedDownloadDirectoryUrl(id))

        val progressAsName = (progress * 100).toInt()
        createPausedDownloadUrl(id, progressAsName).let { newUrl ->
            return data.writeToURL(newUrl, true)
        }
    }

    fun removeDownload(id: String, url: NSURL = getCompletedLocalUrl(id)): Boolean {
        return fileManager.removeItemAtURL(url, error = null)
    }

    fun createPausedDownloadUrl(id: String, progress: Int): NSURL {
        getPausedDownloadDirectoryUrl(id).let { url ->
            url.path?.let {
                if (!fileManager.fileExistsAtPath(it)) {
                    // ..id/
                    fileManager.createDirectoryAtPath(
                        path = it,
                        withIntermediateDirectories = true,
                        attributes = null,
                        error = null
                    )
                }
            }

            // ..id/progress e.g: ..001/45
            return url.URLByAppendingPathComponent(
                "/$progress",
                UTTypeAudio
            )
        }
    }

    fun getDownloadData(id: String): NSData? {
        return getPausedDownloadUrl(id)?.let { NSData.dataWithContentsOfURL(it) }
    }

    fun getFileDownloadState(id: String): DownloadState {
        return if (checkDownloadExists(id)) {
            DownloadState.Completed
        } else {
            getPausedDownloadUrl(id)?.let {
                val progressFromName = (it.lastPathComponent?.toFloat() ?: 0f) / 100
                DownloadState.Paused(progressFromName)
            }
                ?: DownloadState.NotDownloaded
        }
    }

    fun getPausedDownloadUrl(id: String): NSURL? {
        // ..id/progress e.g: ..001/43
        return fileManager.contentsOfDirectoryAtURL(
            url = getPausedDownloadDirectoryUrl(id),
            includingPropertiesForKeys = null,
            options = NSDirectoryEnumerationSkipsHiddenFiles,
            error = null
        )?.firstOrNull() as? NSURL
    }

    fun getPausedDownloadDirectoryUrl(id: String): NSURL {
        return directoryUrl.URLByAppendingPathComponent("$id/", UTTypeDirectory)
    }
}