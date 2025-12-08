package com.m7.mediaplayer.core.di

import org.koin.core.module.Module

expect val platformModule: Module

expect fun Number.format(format: String): String

expect fun Number.formatLocalized(format: String): String