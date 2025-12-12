package com.m7.quranplayer.core.di

import com.m7.quranplayer.player.data.IOSPlayerSource
import com.m7.quranplayer.player.data.PlayerSource
import kotlinx.cinterop.BetaInteropApi
import org.koin.dsl.module
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleMeta
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.preferredLanguages
import platform.Foundation.stringWithFormat

actual val platformModule = module {
    single<PlayerSource> { IOSPlayerSource() }
}

actual fun Int.format(format: String) = NSString.stringWithFormat(format, this)

@OptIn(BetaInteropApi::class)
actual fun Int.localize(format: String): String =
    NSString.create(format = format, locale = NSLocale.current(), this).toString()

fun NSLocaleMeta.current(): NSLocale =
    localeWithLocaleIdentifier(preferredLanguages().first() as String)