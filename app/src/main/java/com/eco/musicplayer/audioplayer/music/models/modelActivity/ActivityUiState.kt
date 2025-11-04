package com.eco.musicplayer.audioplayer.music.models.modelActivity

data class ActivityUiState(
    val editTextContent: String = "",
    val toastMessage: String? = null,
    val taskId: Int = 0,
    val instanceId: Int = 0,
    val instanceCount: Int = 0
)
