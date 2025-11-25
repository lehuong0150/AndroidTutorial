package com.eco.musicplayer.audioplayer.music.ads.banner.admob

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import com.eco.musicplayer.audioplayer.music.models.ads.banner.BannerType
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.ads.mediation.admob.AdMobAdapter

class AdmobBanner(private val activity: Activity) {

    private var adView: AdView? = null

    companion object {
        private const val TAG = "AdmobBanner"
    }

    fun loadAd(
        container: ViewGroup,
        adUnitId: String,
        type: BannerType
    ) {

        // Tính AdSize trước
        val adSize = getAdSize(container, type)

        // Tạo AdView mới với AdSize
        adView = AdView(activity).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize)

            // Set listener
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "[$type] Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "[$type] Ad failed to load: ${error.message}")
                }

                override fun onAdOpened() {
                    Log.d(TAG, "[$type] Ad opened")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "[$type] Ad clicked")
                }

                override fun onAdClosed() {
                    Log.d(TAG, "[$type] Ad closed")
                }
            }
        }

        container.removeAllViews()
        container.addView(adView)

        val adRequest = buildAdRequest(type)

        adView?.loadAd(adRequest)
    }

    private fun getAdSize(container: ViewGroup, type: BannerType): AdSize {
        return when (type) {
            BannerType.COLLAPSIBLE -> {
                val widthPx = container.width.toFloat()
                val density = container.resources.displayMetrics.density
                val adWidth = (widthPx / density).toInt()

                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
            }

            BannerType.ADAPTIVE -> {
                val display = activity.windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)

                val widthPx = outMetrics.widthPixels.toFloat()
                val density = outMetrics.density
                val adWidth = (widthPx / density).toInt()

                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
            }

            BannerType.INLINE -> {
                val widthPx = container.width.toFloat()
                val heightPx = container.height.toFloat()
                val density = container.resources.displayMetrics.density

                val adWidth = (widthPx / density).toInt()
                val adHeight = (heightPx / density).toInt()

                AdSize.getInlineAdaptiveBannerAdSize(adWidth, adHeight)
            }
        }
    }

    private fun buildAdRequest(type: BannerType): AdRequest {
        return when (type) {
            BannerType.COLLAPSIBLE -> {
                val extras = Bundle().apply {
                    putString("collapsible", "bottom")
                }

                AdRequest.Builder()
                    .addNetworkExtrasBundle(
                        AdMobAdapter::class.java,
                        extras
                    )
                    .build()
            }

            else -> {
                AdRequest.Builder().build()
            }
        }
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }
}