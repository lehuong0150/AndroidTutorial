package com.eco.musicplayer.audioplayer.music.ads.reward

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.eco.musicplayer.audioplayer.music.ads.MainAdsActivity
import com.eco.musicplayer.audioplayer.music.ads.interstitial.InterstitialAdManager
import com.eco.musicplayer.audioplayer.music.ads.interstitial.MainInterstitialAdActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainRewardAdBinding
import com.eco.musicplayer.audioplayer.music.utils.ButtonState
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem

class MainRewardAdActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainRewardAdBinding.inflate(layoutInflater)
    }
    private var isAdShowing = false
    private lateinit var rewardedAdManager: RewardedAdManager
    private lateinit var rewardedInterstitialManager: RewardedInterstitialAdManager
    private val RANDOM_AD_CHANCE = 0.9
    private var pendingNavigation: Intent? = null
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

        rewardedAdManager = RewardedAdManager(this)
        rewardedInterstitialManager = RewardedInterstitialAdManager(this)

        rewardedAdManager.adListener = rewardedAdListener
        rewardedInterstitialManager.adListener = interstitialListener

        binding.tvCoin.visibility = View.GONE
        binding.btViewAd.isEnabled = false
        binding.btGoToActivity.isEnabled = false

        rewardedAdManager.loadAd()
        rewardedInterstitialManager.loadAd()

        binding.btViewAd.setOnClickListener {
            handleNextButtonClick()
        }
        binding.btGoToActivity.setOnClickListener {
            handleGoToActivityButtonClick()
        }
    }

    private fun handleGoToActivityButtonClick() {
        navigateWithPossibleAd(Intent(this, MainAdsActivity::class.java))
    }

    private fun navigateWithPossibleAd(intent: Intent) {
        pendingNavigation = intent

        Log.d(
            TAG,
            "Đã load sẵn Rewarded Interstitial? -> ${rewardedInterstitialManager.isAdLoaded()}"
        )

        if (Math.random() < RANDOM_AD_CHANCE && rewardedInterstitialManager.isAdLoaded()) {
            Toast.makeText(this, "Xem quảng cáo nhanh để tiếp tục nào!", Toast.LENGTH_SHORT).show()

            showPreScreenDialog()
        } else {
            if (!rewardedInterstitialManager.isAdLoaded()) {
                Log.d(TAG, "KHÔNG hiện QC vì chưa load xong → Tải lại để lần sau")
                rewardedInterstitialManager.loadAd()
            } else {
                Log.d(TAG, "KHÔNG hiện QC vì không trúng ngẫu nhiên (dù đã load sẵn)")
            }

            Log.d(TAG, "Không hiện Rewarded Interstitial → Chuyển Activity luôn")
            startNavigation()
        }
    }

    private fun startNavigation() {
        pendingNavigation?.let {
            startActivity(it)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            pendingNavigation = null
        }
    }

    private fun handleNextButtonClick() {
        if (rewardedAdManager.isAdLoaded()) {
            rewardedAdManager.showAd()
        } else {
            Log.d(TAG, "Ad is not loaded yet.")
        }
    }

    private val rewardedAdListener = object : AdListener {
        override fun onAdLoaded() {
            binding.btViewAd.isEnabled = true
            isAdShowing = true
        }

        override fun onUserEarnedReward(item: RewardItem) {
            val amount = (item as? RewardItem)?.amount ?: 0
            coin = amount
            totalCoins += amount
            runOnUiThread {
                binding.tvCoin.visibility = View.VISIBLE
                binding.tvCoin.text = getString(R.string.coins_earned_with_total, coin, totalCoins)
            }
        }

        override fun onAdDismissed() {
            isAdShowing = false
            binding.btViewAd.isEnabled = false
            binding.tvCoin.visibility = View.VISIBLE
            binding.tvCoin.text = getString(R.string.coins_earned_with_total, coin, totalCoins)

            rewardedAdManager.loadAd()
        }

        override fun onAdShowed() {}

        override fun onAdFailedToShow(error: String) {
            Log.e(TAG, "Ad failed to show: $error")
            isAdShowing = false

            binding.btViewAd.isEnabled = true
            binding.btViewAd.postDelayed({
                if (!isFinishing) {
                    rewardedAdManager.loadAd()
                }
            }, 3000)
        }
    }
    private val interstitialListener = object : AdListener {
        override fun onAdLoaded() {
            binding.btGoToActivity.isEnabled = true
            isAdShowing = true
        }

        override fun onAdFailedToLoad(error: String) {
            binding.btGoToActivity.isEnabled = true
            if (pendingNavigation != null) {
                startNavigation()
            }
        }

        override fun onUserEarnedReward(item: RewardItem) {
            val amount = (item as? RewardItem)?.amount ?: 0
            totalCoins += amount
            runOnUiThread {
                Toast.makeText(
                    this@MainRewardAdActivity,
                    "+$amount coins từ quảng cáo!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onAdDismissed() {
            startNavigation()
            rewardedInterstitialManager.loadAd()
        }

        override fun onAdShowed() {}

        override fun onAdFailedToShow(error: String) {
            startNavigation()
        }
    }

    private fun showPreScreenDialog() {
        binding.btGoToActivity.isEnabled = false

        AlertDialog.Builder(this)
            .setTitle("Nhận Thưởng Ngay")
            .setMessage("Bạn sẽ xem một quảng cáo ngắn để nhận thưởng và tiếp tục. Bạn có thể chọn BỎ QUA nếu không muốn xem.")
            .setPositiveButton("Xem Quảng Cáo & Tiếp tục") { dialog, which ->
                rewardedInterstitialManager.showAd {
                    startNavigation()
                }
            }
            .setNegativeButton("Bỏ qua") { dialog, which ->
                startNavigation()
            }
            .setOnDismissListener {
                binding.btGoToActivity.isEnabled = true
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        rewardedInterstitialManager.onActivityResumed(this)
    }

    override fun onStop() {
        super.onStop()
        rewardedInterstitialManager.onActivityStopped()
    }

    private fun updateButtonState(state: ButtonState) {
        when (state) {
            ButtonState.LOADING -> {
                binding.btGoToActivity.isEnabled = false
                binding.btGoToActivity.text = "Đang tải..."
            }

            ButtonState.READY -> {
                binding.btGoToActivity.isEnabled = true
                binding.btGoToActivity.text = "Go to Activity"
            }
        }
    }

    companion object {
        private const val TAG = "RewardAdActivity"
    }
}