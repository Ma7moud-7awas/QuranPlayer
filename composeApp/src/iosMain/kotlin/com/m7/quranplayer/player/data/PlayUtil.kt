package com.m7.quranplayer.player.data

import com.m7.quranplayer.downloader.data.DownloadManager
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVMetadataCommonIdentifierAssetIdentifier
import platform.AVFoundation.AVMetadataCommonIdentifierTitle
import platform.AVFoundation.AVMetadataItem
import platform.AVFoundation.AVMutableMetadataItem
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.stringValue
import platform.AVKit.externalMetadata
import platform.AVKit.setExternalMetadata
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSURL.Companion.URLWithString
import platform.darwin.NSObjectProtocol

fun PlayerItem.toAVPlayerItem(): AVPlayerItem? {
    return URLWithString(DownloadManager.getDownloadUrl(id))?.let {
        val idMetaData = AVMutableMetadataItem().apply {
            setIdentifier(AVMetadataCommonIdentifierAssetIdentifier)
            setValue(id as NSObjectProtocol)
        }
        val titleMetaData = AVMutableMetadataItem().apply {
            setIdentifier(AVMetadataCommonIdentifierTitle)
            setValue(title as NSObjectProtocol)
        }

        AVPlayerItem(it).also {
            it.setExternalMetadata(listOf(idMetaData, titleMetaData))
        }
    }
}

fun AVPlayerItem.toPlayerItem(): PlayerItem? {
    return (externalMetadata() as? List<AVMetadataItem>)?.let {
        val id = it.single { it.identifier == AVMetadataCommonIdentifierAssetIdentifier }
            .stringValue ?: return null
        val title = it.single { it.identifier == AVMetadataCommonIdentifierTitle }
            .stringValue ?: return null

        PlayerItem(id, title)
    }
}

fun AVPlayerItem.getId(): String? {
    return (externalMetadata() as? List<AVMetadataItem>)
        ?.single { it.identifier == AVMetadataCommonIdentifierAssetIdentifier }
        ?.stringValue
}

@OptIn(ExperimentalForeignApi::class)
fun AVPlayerItem.isItemTimeCompleted() =
    duration().toMilliseconds() == currentTime().toMilliseconds()


@OptIn(ExperimentalForeignApi::class)
fun CValue<CMTime>.toMilliseconds(): Long {
    return (this.toSeconds() * 1000).toLong()
}

@OptIn(ExperimentalForeignApi::class)
fun CValue<CMTime>.toSeconds(): Double =
    CMTimeGetSeconds(this)