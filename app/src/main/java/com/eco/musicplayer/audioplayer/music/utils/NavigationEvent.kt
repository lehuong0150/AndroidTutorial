package com.eco.musicplayer.audioplayer.music.utils

sealed class NavigationEvent {
    data class OpenStandardActivity(val taskId: Int, val instanceCount: Int) : NavigationEvent()
    object OpenSingleTopActivity : NavigationEvent()
    object OpenSingleTaskActivity : NavigationEvent()
    object OpenSingleInstanceActivity : NavigationEvent()
    data class OpenIntentFlagActivity(val mode: String) : NavigationEvent()
}