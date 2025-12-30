package com.m7.quranplayer.core

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import com.m7.quranplayer.core.ui.App
import com.m7.quranplayer.player.data.PlayerProvider
import com.m7.quranplayer.player.domain.model.PlayerAction

class MainActivity : AppCompatActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var playerCenterAction: PlayerAction? by remember { mutableStateOf(null) }
            var currentChapterId: String? by remember { mutableStateOf(null) }

            PlayerProvider.buildForwardingPlayer(
                this,
                onPrevious = {
                    playerCenterAction = currentChapterId?.let { PlayerAction.Previous.WithId(it) }
                        ?: PlayerAction.Previous
                },
                onNext = {
                    playerCenterAction = currentChapterId?.let { PlayerAction.Next.WithId(it) }
                        ?: PlayerAction.Next
                }
            )

            App(
                onLanguageChanged = { langCode ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(langCode)
                    )
                },
                playerCenterAction = { playerCenterAction },
                onStateChanged = { _, chapter ->
                    currentChapterId = chapter?.id
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}