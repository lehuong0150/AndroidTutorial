package com.eco.musicplayer.audioplayer.music.models.event

data class DownloadProgressEvent(
    val downloadId: Int,
    val progress: Int,
    val fileName: String
)
