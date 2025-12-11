package com.m7.quranplayer.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    secondary = GreenGrey,
    tertiary = Pink80,
    primaryContainer = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Pink40,
    primaryContainer = Color.White,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranPlayerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    CompositionLocalProvider(LocalLayoutDirection provides Locale.current.direction) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = Typography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

val Locale.direction: LayoutDirection
    get() = when (language) {
        "ar" -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }