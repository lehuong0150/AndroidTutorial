package com.eco.musicplayer.audioplayer.music.ads

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainAdsBinding
import com.eco.musicplayer.audioplayer.music.models.ads.banner.BannerType
import com.eco.musicplayer.audioplayer.music.utils.NetworkMonitor
import com.google.android.gms.ads.MobileAds

class MainAdsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainAdsBinding.inflate(layoutInflater)
    }
    private val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(this)
    }
    private var hasLoadedAds = false

    companion object {
        private const val TAG = "BannerDemo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }

        networkMonitor.startMonitoring {
            runOnUiThread {
                if (!hasLoadedAds) {
                    Log.d(TAG, "Network available, loading ads...")
                    loadAllBanners()
                }
            }
        }

        MobileAds.initialize(this) { initStatus ->
            if (networkMonitor.isNetworkAvailable()) {
                loadAllBanners()
            } else {
                Log.d(TAG, "No network available, waiting for connection...")
            }
        }
    }

    private fun loadAllBanners() {
        if (hasLoadedAds) {
            Log.d(TAG, "Ads already loaded, skipping...")
            return
        }
        // Test Ad Unit ID của Google
        val testAdUnitId = "ca-app-pub-3940256099942544/9214589741"

        Log.d(TAG, "Loading ADAPTIVE banner at TOP")
        binding.bannerAdaptiveTop.loadBanner(
            activity = this,
            adUnitId = testAdUnitId,
            type = BannerType.ADAPTIVE
        )

        Log.d(TAG, "Loading COLLAPSIBLE banner at BOTTOM")
        binding.bannerCollapsible.loadBanner(
            activity = this,
            adUnitId = testAdUnitId,
            type = BannerType.COLLAPSIBLE
        )

        Log.d(TAG, "Loading INLINE banner in content")
        binding.bannerInline.loadBanner(
            activity = this,
            adUnitId = testAdUnitId,
            type = BannerType.INLINE
        )
        hasLoadedAds = true
    }

    override fun onDestroy() {
        binding.bannerAdaptiveTop.destroy()
        binding.bannerCollapsible.destroy()
        binding.bannerInline.destroy()
        super.onDestroy()
    }
}