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

    fun loadAd() {
        if (interstitialAd != null) {
            Log.d(TAG, "Ad already loaded, skipping")
            return
        }

        Log.d(TAG, "Start loading ad...")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.i(TAG, "✓ Ad loaded successfully")
                setupAdCallbacks(ad)
                adListener?.onAdLoaded()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.e(TAG, "✗ Load failed: ${error.message}")
                adListener?.onAdFailedToLoad(error.message)
            }
        })
    }

    private fun setupAdCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                interstitialAd = null
                adListener?.onAdDismissed()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "✗ Show failed: ${error.message}")
                interstitialAd = null
                adListener?.onAdFailedToShow(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "✓ Ad showing")
                adListener?.onAdShowed()
            }
        }
    }

    // Trả về true nếu ĐÃ HIỆN quảng cáo, false nếu chưa sẵn sàng
    fun showAdIfAvailable(activity: Activity): Boolean {
        return if (interstitialAd != null) {
            Log.d(TAG, "Showing ad now")
            interstitialAd?.show(activity)
            true
        } else {
            Log.w(TAG, "Ad not ready yet")
            loadAd()
            false
        }
    }

    companion object {
        private const val TAG = "InterstitialAdMgr"
    }
}