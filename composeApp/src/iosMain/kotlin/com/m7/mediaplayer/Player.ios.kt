package com.m7.mediaplayer

import platform.UIKit.UIDevice

class IOSPlayer: Player {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlayer(): Player = IOSPlayer()