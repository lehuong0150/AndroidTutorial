package com.eco.musicplayer.audioplayer.music.utils

import com.eco.musicplayer.audioplayer.music.models.modelActivity.BundleData

sealed class NavigationEvent {
    data class OpenStandardActivity(
        val taskId: Int,
        val instanceCount: Int,
        val bundleData: BundleData,
        val method: String
    ) : NavigationEvent()

    object OpenSingleTopActivity : NavigationEvent()
    object OpenSingleTaskActivity : NavigationEvent()
    object OpenSingleInstanceActivity : NavigationEvent()
    data class OpenIntentFlagActivity(val mode: String) : NavigationEvent()
}