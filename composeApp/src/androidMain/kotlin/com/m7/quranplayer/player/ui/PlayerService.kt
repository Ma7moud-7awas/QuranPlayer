package com.m7.quranplayer.player.ui

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.m7.quranplayer.R
import org.koin.android.ext.android.inject

@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService() {

    private val player: Player by inject()
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val prevButton = CommandButton.Builder(CommandButton.ICON_PREVIOUS)
            .setDisplayName(getString(R.string.previous))
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .setSlots(CommandButton.SLOT_BACK)
            .build()

        val nextButton = CommandButton.Builder(CommandButton.ICON_NEXT)
            .setDisplayName(getString(R.string.next))
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setMediaButtonPreferences(ImmutableList.of(prevButton, nextButton))
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    fun release() {
        player.release()
        mediaSession?.release()
        mediaSession = null
    }
}