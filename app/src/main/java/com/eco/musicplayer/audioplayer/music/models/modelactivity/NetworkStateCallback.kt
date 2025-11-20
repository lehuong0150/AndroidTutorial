package com.eco.musicplayer.audioplayer.music.models.modelactivity

interface NetworkStateCallback {
    fun onNetworkAvailable(networkType: String)
    fun onNetworkLost()
}