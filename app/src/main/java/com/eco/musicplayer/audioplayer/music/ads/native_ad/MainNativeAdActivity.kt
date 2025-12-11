package com.eco.musicplayer.audioplayer.music.ads.native_ad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.google.android.gms.ads.AdListener
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainNativeAdBinding
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class MainNativeAdActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainNativeAdBinding.inflate(layoutInflater) }
    private var currentNativeAd: NativeAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadNativeAd()
    }

    private fun loadNativeAd() {
        val adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/1044960115")
            .forNativeAd { nativeAd ->
                Log.d(TAG, "loadNativeAd: successfully")
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd
                displayNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Native Ad failed to load: ${adError.message}")
                    Log.e(TAG, "Error code: ${adError.code}, Domain: ${adError.domain}")
                    binding.layoutNativeAd.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded callback")
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    Log.d(TAG, "Native Ad opened")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.d(TAG, "Native Ad clicked")
                }
            }).withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            ).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun displayNativeAd(nativeAd: NativeAd) {
        val adView = binding.layoutNativeAd
        adView.mediaView = binding.layoutNativeAd.findViewById(R.id.adMedia)
        adView.headlineView = binding.layoutNativeAd.findViewById(R.id.adHeadline)
        adView.adChoicesView = binding.layoutNativeAd.findViewById(R.id.adChoices)
        adView.bodyView = binding.layoutNativeAd.findViewById(R.id.adBody)
        if (nativeAd.body != null) {
            (adView.bodyView as TextView).text = nativeAd.body
            adView.bodyView?.visibility = View.GONE
        } else {
            adView.bodyView?.visibility = View.GONE
        }

        adView.callToActionView = binding.adCallToAction
        if (nativeAd.callToAction != null) {
            (adView.callToActionView as Button).text = nativeAd.callToAction
            adView.callToActionView?.visibility = View.VISIBLE
        } else {
            adView.callToActionView?.visibility = View.GONE
        }

        adView.iconView = binding.adAppIcon
        if (nativeAd.icon != null) {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        } else {
            adView.iconView?.visibility = View.GONE
        }

        adView.starRatingView = binding.adStars
        if (nativeAd.starRating != null) {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        } else {
            adView.starRatingView?.visibility = View.GONE
        }

        adView.advertiserView = binding.adAdvertiser
        if (nativeAd.advertiser != null) {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        } else {
            adView.advertiserView?.visibility = View.GONE
        }

        adView.storeView = binding.adStore
        if (nativeAd.store != null) {
            (adView.storeView as TextView).text = nativeAd.store
            adView.storeView?.visibility = View.VISIBLE
        } else {
            adView.storeView?.visibility = View.GONE
        }

        adView.priceView = binding.adPrice
        if (nativeAd.price != null) {
            (adView.priceView as TextView).text = nativeAd.price
            adView.priceView?.visibility = View.VISIBLE
        } else {
            adView.priceView?.visibility = View.GONE
        }

        if (nativeAd.adChoicesInfo != null) {
            adView.adChoicesView?.visibility = View.VISIBLE
            Log.d(TAG, "displayNativeAd: adChoicesInfo")
        } else {
            adView.adChoicesView?.visibility = View.GONE
            Log.d(TAG, "displayNativeAd: adChoicesInfo null")
        }
        adView.setNativeAd(nativeAd)
    }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainNativeAdActivity"
    }
}