package com.eco.musicplayer.audioplayer.music.models.overlay

data class AppInfo(
    val packageName: String,
    val appName: String,
    var isLocked: Boolean = false
)
