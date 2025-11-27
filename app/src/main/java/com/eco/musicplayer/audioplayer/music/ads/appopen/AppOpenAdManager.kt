package com.eco.musicplayer.audioplayer.music.ads.appopen

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager(private val context: Context) {

    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private var loadTime: Long = 0
    private var lastShowTime: Long = 0
    private var pendingActivity: Activity? = null
    private val SHOW_COOLDOWN_MS = 30_000L

    private fun wasLoadTimeLessThan4HoursAgo(): Boolean {
        return Date().time - loadTime < 14400000
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThan4HoursAgo()
    }

    private fun hasPassedCooldown(): Boolean {
        if (lastShowTime == 0L) return true

        val timeSinceLastShow = Date().time - lastShowTime
        return timeSinceLastShow >= SHOW_COOLDOWN_MS
    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        Log.d(TAG, "Loading ad...")

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.i(TAG, "Ad loaded")

                    pendingActivity?.let { activity ->
                        Log.d(TAG, "Auto showing ad for pending activity")
                        showAdIfAvailable(activity)
                        pendingActivity = null
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    appOpenAd = null
                    pendingActivity = null
                    Log.e(TAG, "Load failed: ${loadAdError.message}")
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) {
            Log.d(TAG, "Ad is showing, skip")
            return
        }

        if (!hasPassedCooldown()) {
            Log.d(TAG, "Cooldown not passed (${(Date().time - lastShowTime) / 1000}s), skip")
            return
        }

        val activityName = activity.javaClass.simpleName
        val shouldSkip = activityName == "SplashActivity" ||
                activityName.contains("InterstitialActivity") ||
                activityName.contains("RewardedActivity") ||
                activityName == "AdActivity"

        if (shouldSkip) {
            Log.d(TAG, "Skip in ad screen: $activityName")
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "Ad not available, loading...")
            pendingActivity = activity
            loadAd()
            return
        }

        Log.d(TAG, "Showing ad")
        isShowingAd = true
        lastShowTime = Date().time

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Show failed: ${error.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "Ad showed")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad impression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Ad clicked")
            }
        }

        appOpenAd?.show(activity)
    }

    companion object {
        private const val TAG = "AppOpenAd"
    }
}
