package com.m7.quranplayer.core

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import androidx.media3.common.util.UnstableApi
import com.m7.quranplayer.core.ui.App

class MainActivity : AppCompatActivity() {

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App(
                onLanguageChanged = { langCode ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(langCode)
                    )
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