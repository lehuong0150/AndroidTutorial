package com.eco.musicplayer.audioplayer.music.ads.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date


class InterstitialAdManager(private val context: Context) {

    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private var interstitialAd: InterstitialAd? = null
    var adListener: AdListener? = null

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
            Log.d(TAG, "Ad is currently showing or was interrupted, skipping load")
            return
        }

        if (interstitialAd != null) {
            Log.d(TAG, "Ad already loaded, skipping")
            return
        }

        Log.d(TAG, "Start loading ad...")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                loadTime = Date().time
                Log.i(TAG, "Ad loaded successfully")
                setupAdCallbacks(ad)
                adListener?.onAdLoaded()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                loadTime = 0
                Log.e(TAG, "Load failed: ${error.message}")
                adListener?.onAdFailedToLoad(error.message)
            }
        })
    }

    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                val adDuration = if (adShowStartTime > 0) {
                    (Date().time - adShowStartTime) / 1000
                } else 0

                isAdShowing = false
                adWasInterrupted = false
                adShowStartTime = 0
                interstitialAd = null
                loadTime = 0
                lastShowTime = Date().time
                currentActivity = null

                adListener?.onAdDismissed()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Show failed: ${error.message}")
                isAdShowing = false
                adWasInterrupted = false
                adShowStartTime = 0
                interstitialAd = null
                loadTime = 0
                currentActivity = null

                adListener?.onAdFailedToShow(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "Ad showing")
                isAdShowing = true
                adShowStartTime = Date().time
                adListener?.onAdShowed()
            }
        }
    }

    fun showAdIfAvailable(activity: Activity): Boolean {
        if (isAdShowing) {
            Log.d(TAG, "Ad is already showing")
            return true
        }



        if (interstitialAd != null && isAdExpired()) {
            Log.d(TAG, "Ad expired, loading new ad")
            interstitialAd = null
            loadTime = 0
            loadAd()
            return false
        }

        return if (interstitialAd != null) {
            Log.d(TAG, "Showing ad now")
            currentActivity = activity
            interstitialAd?.show(activity)
            true
        } else {
            Log.w(TAG, "Ad not ready yet")
            loadAd()
            false
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
        if (adWasInterrupted && interstitialAd != null) {
            Log.d(TAG, "Activity resumed - attempting to continue showing ad")

            currentActivity = activity

        } else if (isAdShowing) {
            Log.d(TAG, "Activity resumed while ad is still showing")
            currentActivity = activity
        }
    }

    fun isAdCurrentlyShowing(): Boolean = isAdShowing

    fun isAdReady(): Boolean = interstitialAd != null && !isAdShowing && !isAdExpired()

    fun wasAdInterrupted(): Boolean = adWasInterrupted

    fun destroy() {
        interstitialAd = null
        isAdShowing = false
        adWasInterrupted = false
        adShowStartTime = 0
        loadTime = 0
        currentActivity = null
    }

    companion object {
        private const val TAG = "InterstitialAdMgr"
    }
}