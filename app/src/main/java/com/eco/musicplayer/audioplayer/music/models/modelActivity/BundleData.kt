package com.eco.musicplayer.audioplayer.music.models.modelActivity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class BundleData(
    val taskId: Int,
    val instanceLabel: String,
    val startTime: Long,
    val isForeground: Boolean
) : Parcelable, Serializable
