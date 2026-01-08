package com.m7.quranplayer.core.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import quranplayer.composeapp.generated.resources.Res
import quranplayer.composeapp.generated.resources.icomoon
import quranplayer.composeapp.generated.resources.saleem_quran_font

@Composable
fun icomoonFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.icomoon,
        weight = FontWeight.Bold
    ),
)

@Composable
fun saleemFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.saleem_quran_font,
        weight = FontWeight.Bold
    ),
)

val appFontFamily: FontFamily
    @Composable
    get() = saleemFontFamily()

val chapterTitleTextStyle: TextStyle
    @Composable
    get() = if (Locale.current.language == "ar")
        Typography.titleLarge.copy(fontFamily = icomoonFontFamily())
    else Typography.titleLarge

val digitsTextStyle: TextStyle
    @Composable
    get() = if (Locale.current.language == "ar")
        TextStyle.Default else LocalTextStyle.current

val Typography: Typography
    @Composable
    get() =
        Typography(
            displayLarge = Typography().displayLarge.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),
            displayMedium = Typography().displayMedium.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),
            displaySmall = Typography().displaySmall.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),

            headlineLarge = Typography().headlineLarge.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),
            headlineMedium = Typography().headlineMedium.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),
            headlineSmall = Typography().headlineSmall.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp,
            ),

            titleLarge = Typography().titleLarge.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            titleMedium = Typography().titleMedium.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            titleSmall = Typography().titleSmall.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),

            bodyLarge = Typography().bodyLarge.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            bodyMedium = Typography().bodyMedium.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            bodySmall = Typography().bodySmall.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),

            labelLarge = Typography().labelLarge.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            labelMedium = Typography().labelMedium.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            ),
            labelSmall = Typography().labelSmall.copy(
                fontFamily = appFontFamily,
                letterSpacing = 1.sp
            )
        )