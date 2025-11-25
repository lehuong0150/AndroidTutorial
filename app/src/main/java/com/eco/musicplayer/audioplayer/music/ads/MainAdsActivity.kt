package com.eco.musicplayer.audioplayer.music.ads

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainAdsBinding
import com.eco.musicplayer.audioplayer.music.models.ads.banner.BannerType
import com.google.android.gms.ads.MobileAds

class MainAdsActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainAdsBinding.inflate(layoutInflater)
    }

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

        // Khởi tạo Mobile Ads SDK
        MobileAds.initialize(this) { initStatus ->
            Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap}")
            loadAllBanners()
        }
    }

    private fun loadAllBanners() {
        // Test Ad Unit ID của Google
        val testAdUnitId = "ca-app-pub-3940256099942544/6300978111"

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

        Log.d(TAG, "Loading INLINE 250dp banner in content")
        binding.bannerInline250.loadBanner(
            activity = this,
            adUnitId = testAdUnitId,
            type = BannerType.INLINE,
            heightDp = 250
        )

        Log.d(TAG, "Loading INLINE 150dp banner in content")
        binding.bannerInline150.loadBanner(
            activity = this,
            adUnitId = testAdUnitId,
            type = BannerType.INLINE,
            heightDp = 150
        )
    }

    override fun onDestroy() {
        // Hủy tất cả banner khi destroy activity
        Log.d(TAG, "Destroying all banners")
        binding.bannerAdaptiveTop.destroy()
        binding.bannerCollapsible.destroy()
        binding.bannerInline250.destroy()
        binding.bannerInline150.destroy()
        super.onDestroy()
    }
}