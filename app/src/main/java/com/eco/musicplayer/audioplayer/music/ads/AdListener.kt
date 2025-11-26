package com.eco.musicplayer.audioplayer.music.ads

import com.google.android.gms.ads.rewarded.RewardItem

interface AdListener {
    fun onAdDismissed()
    fun onAdShowed()
    fun onAdFailedToShow(error: String)
    fun onAdLoaded() {}
    fun onAdFailedToLoad(error: String) {}
    fun onUserEarnedReward(item: RewardItem)

}