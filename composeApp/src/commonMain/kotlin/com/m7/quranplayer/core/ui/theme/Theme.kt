package com.m7.quranplayer.core.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Orange,
    background = LightGreenGrey,
    primaryContainer = Color.White
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuranPlayerTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides Locale.current.direction) {
        MaterialExpressiveTheme(
            colorScheme = LightColorScheme,
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