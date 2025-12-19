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

class InterstitialAdManager(private val context: Context) {
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private var interstitialAd: InterstitialAd? = null
    var adListener: AdListener? = null

    private var lastShowTime: Long = 0
    private val SHOW_COOLDOWN_MS = 30_000L  // 30 giây
    private var loadTime: Long = 0
    private val AD_EXPIRY_TIME_MS = 3_600_000L  // 1 giờ
    private var isAdShowing = false

    private fun isAdExpired(): Boolean {
        if (loadTime == 0L) return true
        return System.currentTimeMillis() - loadTime > AD_EXPIRY_TIME_MS
    }

    fun hasPassedCooldown(): Boolean {
        if (lastShowTime == 0L) return true
        val elapsed = System.currentTimeMillis() - lastShowTime
        return elapsed >= SHOW_COOLDOWN_MS
    }

    fun loadAd() {
        if (isAdShowing || interstitialAd != null) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                loadTime = System.currentTimeMillis()
                setupAdCallbacks(ad)
                adListener?.onAdLoaded()
                Log.d("AdManager", "Ad loaded thành công")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                adListener?.onAdFailedToLoad(error.message)
                Log.e("AdManager", "Load ad thất bại: ${error.message}")
            }
        })
    }

    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                interstitialAd = null
                lastShowTime = System.currentTimeMillis()
                val wasDismissedByBackPress = ad.responseInfo.responseExtras.getBoolean("googlesdk_ad_dismissed_by_back_press", false)

                if (!wasDismissedByBackPress) {
                    adListener?.onAdDismissed()
                } else {
                    Log.d("AdManager", "Quảng cáo bị đóng bởi nút Back → không chuyển màn")
                }

                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isAdShowing = false
                interstitialAd = null
                adListener?.onAdFailedToShow(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
                adListener?.onAdShowed()
            }
        }
    }

    fun showAdIfAvailable(activity: Activity): Boolean {
        if (isAdShowing) return true

        if (!hasPassedCooldown()) {
            Log.d("AdManager", "Cooldown chưa đủ (${System.currentTimeMillis() - lastShowTime}ms) → Không show ad")
            return false
        }

        if (interstitialAd != null && isAdExpired()) {
            interstitialAd = null
            loadAd()
            return false
        }

        return if (interstitialAd != null) {
            Log.d("AdManager", "Show ad thành công (đủ cooldown)")
            interstitialAd?.show(activity)
            true
        } else {
            loadAd()
            false
        }
    }

    fun destroy() {
        interstitialAd = null
        adListener = null
    }
}