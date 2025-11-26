package com.eco.musicplayer.audioplayer.music.ads.reward

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainRewardAdBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem

class MainRewardAdActivity : AppCompatActivity(), AdListener {

    private val binding by lazy {
        ActivityMainRewardAdBinding.inflate(layoutInflater)
    }
    private var isAdShowing = false
    private lateinit var adManager: RewardedAdManager
    private var totalCoins = 0
    private var coin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MobileAds.initialize(this) {
            Log.d(TAG, "MobileAds initialized")
        }

        adManager = RewardedAdManager(this)
        adManager.adListener = this

        binding.tvCoin.visibility = View.GONE

        // Disable button khi đang load
        binding.btViewAd.isEnabled = false

        adManager.loadAd()

        binding.btViewAd.setOnClickListener {
            handleNextButtonClick()
        }
    }

    private fun handleNextButtonClick() {
        if (adManager.isAdLoaded()) {
            adManager.showAd()
        } else {
            Log.d(TAG, "Ad is not loaded yet.")
        }
    }

    override fun onAdDismissed() {
        Log.d(TAG, "Ad was dismissed.")
        isAdShowing = false

        binding.btViewAd.isEnabled = false
        binding.tvCoin.visibility = View.VISIBLE

        binding.tvCoin.text = getString(R.string.coins_earned_with_total, coin, totalCoins)

        adManager.loadAd()
    }

    override fun onAdShowed() {
        Log.d(TAG, "Ad is showing.")
        isAdShowing = true
    }

    override fun onAdFailedToShow(error: String) {
        Log.e(TAG, "Ad failed to show: $error")
        isAdShowing = false

        binding.btViewAd.isEnabled = true
    }

    override fun onUserEarnedReward(item: RewardItem) {
        Log.d(TAG, "coins" + item.amount)
        totalCoins += item.amount
        coin = item.amount
    }

    override fun onAdLoaded() {
        binding.btViewAd.isEnabled = true
        Log.d(TAG, "Ad loaded, button enabled")
    }

    companion object {
        private const val TAG = "RewardAdActivity"
    }
}