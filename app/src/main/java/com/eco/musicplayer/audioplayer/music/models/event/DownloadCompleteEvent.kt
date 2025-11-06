package com.eco.musicplayer.audioplayer.music.models.event

data class DownloadCompleteEvent(
    val downloadId: Int,
    val fileName: String,
    val success: Boolean,
    val message: String
)
