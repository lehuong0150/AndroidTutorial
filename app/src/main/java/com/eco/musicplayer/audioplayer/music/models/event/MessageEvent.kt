package com.eco.musicplayer.audioplayer.music.models.event

data class MessageEvent(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
