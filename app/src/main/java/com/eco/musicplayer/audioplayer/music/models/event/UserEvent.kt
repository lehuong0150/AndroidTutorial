package com.eco.musicplayer.audioplayer.music.models.event

data class UserEvent(
    val userId: Int,
    val userName: String,
    val isLoggedIn: Boolean
)