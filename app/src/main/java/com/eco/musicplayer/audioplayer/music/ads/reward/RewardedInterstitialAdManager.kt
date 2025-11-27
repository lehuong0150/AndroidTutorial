package com.eco.musicplayer.audioplayer.music.ads.reward

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class RewardedInterstitialAdManager(private val context: Context) {
    private val TAG = "RewardedInterstitialAdManager"
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    var adListener: AdListener? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

    fun loadAd() {
        rewardedInterstitialAd = null
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedInterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedInterstitialAd = rewardedAd
                    setAdCallbacks(rewardedAd)
                    adListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                    rewardedInterstitialAd = null
                    adListener?.onAdFailedToLoad(adError.message)
                }
            }
        )
    }

    fun setAdCallbacks(rewardedAd: RewardedInterstitialAd) {
        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                rewardedInterstitialAd = null
                loadAd()
                adListener?.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show.")
                rewardedInterstitialAd = null
                adListener?.onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                adListener?.onAdShowed()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }
        }
    }

    fun isAdLoaded() = rewardedInterstitialAd != null

    fun showAd(onReward: (() -> Unit)? = null) {
        rewardedInterstitialAd?.show(context as AppCompatActivity) { rewardItem ->
            adListener?.onUserEarnedReward(rewardItem)
            onReward?.invoke()
        } ?: run {
            Log.d(TAG, "Ad not loaded yet.")
            onReward?.invoke()
        }
    }
}