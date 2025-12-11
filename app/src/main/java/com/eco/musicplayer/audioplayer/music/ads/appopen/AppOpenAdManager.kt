package com.eco.musicplayer.audioplayer.music.ads.appopen

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import android.widget.ProgressBar
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.lang.Exception
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
    private var isFirstLaunch = true
    private var loadingDialog: Dialog? = null

    private val pendingCallbacks = mutableListOf<AdLoadCallback>()

    private fun wasLoadTimeLessThan4HoursAgo(): Boolean {
        return Date().time - loadTime < 14400000
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThan4HoursAgo()
    }

    private fun hasPassedCooldown(): Boolean {
        if (isFirstLaunch) return true
        if (lastShowTime == 0L) return true

        val timeSinceLastShow = Date().time - lastShowTime
        return timeSinceLastShow >= SHOW_COOLDOWN_MS
    }

    private fun createLoadingDialog(context: Context): Dialog {
        return Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

//            val progressBar = ProgressBar(context).apply {
//                isIndeterminate = true
//            }
//            setContentView(progressBar)
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) {
                try {
                    it.dismiss()
                    Log.d(TAG, "Loading dialog dismissed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error dismissing dialog: ${e.message}")
                }
            }
        }
        loadingDialog = null
    }

    fun loadAdWithCallback(callback: AdLoadCallback) {
        if (isLoadingAd) {
            Log.d(TAG, "Already loading ad")
            pendingCallbacks.add(callback)
            return
        }
        if (isAdAvailable()) {
            Log.d(TAG, "Ad already available")
            callback.onAdLoaded()
            return
        }
        pendingCallbacks.add(callback)
        startLoadingAd()
    }

    private fun startLoadingAd() {
        if (isLoadingAd) {
            Log.d(TAG, "Already loading, skip")
            return
        }

        isLoadingAd = true
        Log.d(TAG, "Starting ad load... (${pendingCallbacks.size} callbacks waiting)")

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
                    Log.i(TAG, "Ad loaded successfully")

                    val callbacks = pendingCallbacks.toList()
                    pendingCallbacks.clear()
                    Log.d(TAG, "Notifying ${callbacks.size} callbacks")
                    callbacks.forEach { it.onAdLoaded() }

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
                    Log.e(TAG, "Ad load failed: ${loadAdError.message}")

                    val callbacks = pendingCallbacks.toList()
                    pendingCallbacks.clear()
                    Log.d(TAG, "Notifying ${callbacks.size} callbacks about failure")
                    callbacks.forEach { it.onAdFailedToLoad() }
                }
            }
        )
    }

    //    fun loadAd() {
//        if (isLoadingAd || isAdAvailable()) {
//            return
//        }
//
//        isLoadingAd = true
//        Log.d(TAG, "Loading ad...")
//
//        val request = AdRequest.Builder().build()
//        AppOpenAd.load(
//            context,
//            AD_UNIT_ID,
//            request,
//            object : AppOpenAd.AppOpenAdLoadCallback() {
//                override fun onAdLoaded(ad: AppOpenAd) {
//                    appOpenAd = ad
//                    isLoadingAd = false
//                    loadTime = Date().time
//                    Log.i(TAG, "Ad loaded")
//
//                    pendingActivity?.let { activity ->
//                        Log.d(TAG, "Auto showing ad for pending activity")
//                        showAdIfAvailable(activity)
//                        pendingActivity = null
//                    }
//                }
//
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    isLoadingAd = false
//                    appOpenAd = null
//                    pendingActivity = null
//                    Log.e(TAG, "Load failed: ${loadAdError.message}")
//                }
//            }
//        )
//    }

    fun loadAd() {
        if (isLoadingAd || isAdAvailable()) {
            Log.d(TAG, "Skip loadAd - already loading or available")
            return
        }

        Log.d(TAG, "loadAd() called without callback")
        startLoadingAd()
    }

    fun showAdIfAvailable(activity: Activity, onAdClosed: (() -> Unit)? = null) {
        if (isShowingAd) {
            Log.d(TAG, "Ad is showing, skip")
            onAdClosed?.invoke()
            return
        }

        if (!hasPassedCooldown()) {
            Log.d(TAG, "Cooldown not passed (${(Date().time - lastShowTime) / 1000}s), skip")
            onAdClosed?.invoke()
            return
        }

        val activityName = activity.javaClass.simpleName
        val shouldSkip = activityName == "SplashActivity" ||
                activityName.contains("InterstitialActivity") ||
                activityName.contains("MainAdsActivity") ||
                activityName == "AdActivity"

        if (shouldSkip) {
            Log.d(TAG, "Skip in ad screen: $activityName")
            onAdClosed?.invoke()
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "Ad not available, loading...")
            pendingActivity = activity
            loadAd()
            onAdClosed?.invoke()
            return
        }

        Log.d(TAG, "Showing ad")
        loadingDialog = createLoadingDialog(activity)
        loadingDialog?.show()

        isShowingAd = true
        lastShowTime = Date().time

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                appOpenAd = null
                isShowingAd = false
                isFirstLaunch = false
                dismissLoadingDialog()
                onAdClosed?.invoke()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Show failed: ${error.message}")
                appOpenAd = null
                isShowingAd = false
                isFirstLaunch = false
                onAdClosed?.invoke()
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "Ad showed")
                isFirstLaunch = false
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

//    fun resetFirstLaunch() {
//        isFirstLaunch = true
//    }

    companion object {
        private const val TAG = "AppOpenAd"
    }
}
