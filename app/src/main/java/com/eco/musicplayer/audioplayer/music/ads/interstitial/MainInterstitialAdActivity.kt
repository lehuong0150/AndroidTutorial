package com.eco.musicplayer.audioplayer.music.ads.interstitial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.eco.musicplayer.audioplayer.music.ads.MainAdsActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainInterstitialAdBinding
import com.eco.musicplayer.audioplayer.music.utils.ButtonState
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem

class MainInterstitialAdActivity : AppCompatActivity(), AdListener {

    private val binding by lazy {
        ActivityMainInterstitialAdBinding.inflate(layoutInflater)
    }

    private var isAdShowing = false
    private var hasUserClickedNext = false // Tracking xem user đã click chưa
    private lateinit var adManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo ads
        MobileAds.initialize(this) {
            Log.d(TAG, "MobileAds initialized")
        }

        adManager = InterstitialAdManager(this)
        adManager.adListener = this

        // Hiển thị loading ngay từ đầu
        updateButtonState(ButtonState.LOADING)
        adManager.loadAd()

        // Setup click listener
        binding.btNext.setOnClickListener {
            handleNextButtonClick()
        }
    }

    private fun handleNextButtonClick() {
        if (isAdShowing) {
            Log.d(TAG, "Ad is showing, ignore click")
            return
        }

        hasUserClickedNext = true
        Log.d(TAG, "User clicked Next")

        // Thử show ad
        if (adManager.showAdIfAvailable(this)) {
            Log.d(TAG, "Ad shown immediately")
        } else {
            Log.d(TAG, "Ad not ready, waiting...")
            updateButtonState(ButtonState.LOADING)
        }
    }

    override fun onAdLoaded() {
        Log.d(TAG, "✓ Ad loaded")

        // Bỏ qua nếu activity đang đóng
        if (isFinishing) {
            Log.d(TAG, "Activity finishing, ignore ad loaded")
            return
        }

        if (hasUserClickedNext && !isAdShowing) {
            Log.d(TAG, "User already clicked, showing ad now")
            adManager.showAdIfAvailable(this)
        } else {
            Log.d(TAG, "Ad ready, waiting for user click")
            updateButtonState(ButtonState.READY)
        }
    }

    override fun onUserEarnedReward(item: RewardItem) {
        TODO("Not yet implemented")
    }

    override fun onAdShowed() {
        if (isFinishing) return
        isAdShowing = true
        Log.i(TAG, "✓ Ad showing")
    }

    override fun onAdDismissed() {
        if (isFinishing) return
        isAdShowing = false
        Log.d(TAG, "Ad dismissed")
        goToNextLevel()
    }

    override fun onAdFailedToShow(error: String) {
        if (isFinishing) return
        isAdShowing = false
        Log.e(TAG, "Show failed: $error")
        goToNextLevel()
    }

    private fun goToNextLevel() {
        if (!isFinishing) {
            Log.d(TAG, "Moving to next screen")
            adManager.adListener = null
            startActivity(Intent(this, MainAdsActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adManager.adListener = null
        Log.d(TAG, "Activity destroyed")
    }

    private fun updateButtonState(state: ButtonState) {
        when (state) {
            ButtonState.LOADING -> {
                binding.btNext.isEnabled = false
                binding.btNext.text = "Đang tải..."
            }

            ButtonState.READY -> {
                binding.btNext.isEnabled = true
                binding.btNext.text = "Tiếp tục"
            }
        }
    }

    companion object {
        private const val TAG = "InterstitialAdActivity"
    }
}