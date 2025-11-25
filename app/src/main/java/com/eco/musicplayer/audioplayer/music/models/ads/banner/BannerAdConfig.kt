package com.eco.musicplayer.audioplayer.music.models.ads.banner

data class BannerAdConfig(
    val enable: Boolean = false,
    val bannerType: String? = null,
    val adUnitId: String? = null,
    val intervalSeconds: Int = 60,
    val heightSdp: Int? = null,
    val screenPercent: Int? = null
)
