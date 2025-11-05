package com.eco.musicplayer.audioplayer.music.models.modelActivity

interface NetworkStateCallback {
    fun onNetworkAvailable(networkType: String)
    fun onNetworkLost()
}