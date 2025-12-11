package com.eco.musicplayer.audioplayer.music.ads.reward

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.eco.musicplayer.audioplayer.music.ads.interstitial.InterstitialAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.util.Date

class RewardedInterstitialAdManager(private val context: Context) {
    private val TAG = "RewardedInterstitialAdManager"
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    var adListener: AdListener? = null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

    private var lastShowTime: Long = 0
    private val SHOW_COOLDOWN_MS = 30_000L

    private var loadTime: Long = 0
    private val AD_EXPIRY_TIME_MS = 3_600_000L
    private var adShowStartTime: Long = 0
    private var adWasInterrupted = false
    private var currentActivity: Activity? = null
    private var isAdShowing = false

    private fun isAdExpired(): Boolean {
        if (loadTime == 0L) return true
        return Date().time - loadTime > AD_EXPIRY_TIME_MS
    }

    private fun hasPassedCooldown(): Boolean {
        if (lastShowTime == 0L) return true

        val timeSinceLastShow = Date().time - lastShowTime
        return timeSinceLastShow >= SHOW_COOLDOWN_MS
    }

    fun loadAd() {
        if (isAdShowing || adWasInterrupted) {
            return
        }

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
                    loadTime = Date().time
                    setAdCallbacks(rewardedAd)
                    adListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                    rewardedInterstitialAd = null
                    loadTime = 0
                    adListener?.onAdFailedToLoad(adError.message)
                }
            }
        )
    }

    fun setAdCallbacks(rewardedAd: RewardedInterstitialAd) {
        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                val adDuration = if (adShowStartTime > 0) {
                    (Date().time - adShowStartTime) / 1000
                } else 0

                isAdShowing = false
                adWasInterrupted = false
                adShowStartTime = 0
                loadTime = 0
                lastShowTime = Date().time
                currentActivity = null
                rewardedInterstitialAd = null
                loadAd()
                adListener?.onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show.")
                isAdShowing = false
                adWasInterrupted = false
                adShowStartTime = 0
                loadTime = 0
                currentActivity = null
                rewardedInterstitialAd = null
                adListener?.onAdFailedToShow(adError.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
                adShowStartTime = Date().time
                isAdShowing = true
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
        if (isAdShowing) {
            return
        }
        if (!hasPassedCooldown()) {
            return
        }
        rewardedInterstitialAd?.show(context as AppCompatActivity) { rewardItem ->
            adListener?.onUserEarnedReward(rewardItem)
            onReward?.invoke()
        } ?: run {
            Log.d(TAG, "Ad not loaded yet.")
            onReward?.invoke()
        }
    }

    fun onActivityStopped() {
        if (isAdShowing) {
            Log.d(TAG, "Activity stopped while ad is showing - ad will be interrupted")
            adWasInterrupted = true

            val currentDuration = if (adShowStartTime > 0) {
                (Date().time - adShowStartTime) / 1000
            } else 0
            Log.d(TAG, "Ad was showing for ${currentDuration}s before interruption")
        }
    }

    fun onActivityResumed(activity: Activity) {
        if (adWasInterrupted && rewardedInterstitialAd != null) {
            Log.d(TAG, "Activity resumed - attempting to continue showing ad")

            currentActivity = activity

        } else if (isAdShowing) {
            Log.d(TAG, "Activity resumed while ad is still showing")
            currentActivity = activity
        }
    }
    fun destroy() {
        rewardedInterstitialAd = null
        isAdShowing = false
        adWasInterrupted = false
        adShowStartTime = 0
        loadTime = 0
        currentActivity = null
    }
}