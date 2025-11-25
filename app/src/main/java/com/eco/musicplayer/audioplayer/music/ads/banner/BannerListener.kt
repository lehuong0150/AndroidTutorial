package com.eco.musicplayer.audioplayer.music.ads.banner


interface BannerListener {
    fun onBannerLoaded() {}
    fun onBannerFailed(error: String) {}
    fun onBannerOpened() {}
    fun onBannerClosed() {}
    fun onBannerClicked() {}
    fun onBannerImpression() {}
}
