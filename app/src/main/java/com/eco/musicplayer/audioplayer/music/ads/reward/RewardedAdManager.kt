package com.eco.musicplayer.audioplayer.music.ads.reward

import android.content.Context
import android.util.Log
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private val TAG = "RewardedAdManager"
    private var rewardedAd: RewardedAd? = null
    var adListener: AdListener? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    fun loadAd() {
        rewardedAd = null
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                setAdCallbacks(ad)

                (context as? MainRewardAdActivity)?.onAdLoaded()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load: ${adError.message}")
                adListener?.onAdFailedToShow(adError.message)
            }
        })
    }

    fun isAdLoaded() = rewardedAd != null

    fun showAd() {
        rewardedAd?.show(context as MainRewardAdActivity) { rewardItem ->
            adListener?.onUserEarnedReward(rewardItem)
        } ?: Log.d(TAG, "Ad not loaded yet.")
    }

    private fun setAdCallbacks(ad: RewardedAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
                adListener?.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
                adListener?.onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                adListener?.onAdShowed()
            }
        }
    }
}