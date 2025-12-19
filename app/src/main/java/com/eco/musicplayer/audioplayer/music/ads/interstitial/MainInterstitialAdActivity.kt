package com.eco.musicplayer.audioplayer.music.ads.interstitial

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.ads.AdListener
import com.eco.musicplayer.audioplayer.music.ads.MainAdsActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainInterstitialAdBinding
import com.eco.musicplayer.audioplayer.music.utils.ButtonState
import com.google.android.gms.ads.rewarded.RewardItem

private const val TAG = "InterstitialFlow"

class MainInterstitialAdActivity : AppCompatActivity(), AdListener {

    private val binding by lazy { ActivityMainInterstitialAdBinding.inflate(layoutInflater) }
    private lateinit var adManager: InterstitialAdManager
    private var hasUserClickedNext = false
    private var progressDialog: Dialog? = null

    private var wasPausedByChildActivity = false
    private var pendingAutoShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        enableEdgeToEdge()
        setContentView(binding.root)

        adManager = InterstitialAdManager(this)
        adManager.adListener = this
        adManager.loadAd()

        updateButtonState(ButtonState.LOADING)

        binding.btNext.setOnClickListener {
            Log.d(TAG, "User nhấn nút 'Tiếp tục'")
            hasUserClickedNext = true

            // THAY ĐỔI CHÍNH TẠI ĐÂY:
            if (adManager.hasPassedCooldown()) {
                // Đủ cooldown → cố gắng show ad (hiện loading, chờ load nếu cần)
                showLoadingDialog()
                attemptShowAd()
            } else {
                // Chưa đủ cooldown → bỏ qua quảng cáo, chuyển thẳng luôn
                Log.d(TAG, "Chưa đủ cooldown → bỏ qua quảng cáo, chuyển thẳng sang màn tiếp theo")
                goToNextLevel()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")

        if (!isFinishing) {
            wasPausedByChildActivity = true
            Log.d(TAG, "Đang chuyển sang Activity khác → đánh dấu wasPausedByChildActivity = true")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        if (wasPausedByChildActivity && !hasUserClickedNext) {
            pendingAutoShow = true
            // Vẫn hiện loading ngắn để chờ ad load (nếu đủ cooldown sẽ show)
            showLoadingDialog()
            attemptShowAd()
        } else {
            Log.d(TAG, "Không kích hoạt auto show: lần đầu hoặc resume từ background")
        }
    }

    private fun attemptShowAd() {
        val shown = adManager.showAdIfAvailable(this)
        if (shown) {
            Log.d(TAG, "Quảng cáo SHOW thành công ${if (pendingAutoShow) "(tự động khi back)" else "(user nhấn)"}")
            pendingAutoShow = false
            wasPausedByChildActivity = false
        } else {
            Log.d(TAG, "Không show được ad (chưa đủ cooldown hoặc chưa load)")

            if (pendingAutoShow) {
                // Back lại nhưng chưa đủ cooldown → ẩn loading sau 3s, cho phép nhấn tay
                binding.root.postDelayed({
                    if (progressDialog?.isShowing == true) {
                        hideLoadingDialog()
                        updateButtonState(ButtonState.READY)
                    }
                }, 3000)
            }
            // Nếu là user nhấn → đã xử lý ở onClick (không vào đây nếu chưa đủ cooldown)
        }
    }

    override fun onAdLoaded() {
        Log.d(TAG, "onAdLoaded()")
        updateButtonState(ButtonState.READY)

        if (pendingAutoShow || (hasUserClickedNext && adManager.hasPassedCooldown())) {
            attemptShowAd()
        }
    }

    override fun onAdDismissed() {
        Log.d(TAG, "onAdDismissed()")
        hideLoadingDialog()
        pendingAutoShow = false
        wasPausedByChildActivity = false
        goToNextLevel()
    }

    override fun onAdShowed() {
        Log.d(TAG, "onAdShowed()")
    }

    override fun onAdFailedToLoad(error: String) {
        Log.e(TAG, "onAdFailedToLoad: $error")
        updateButtonState(ButtonState.READY)
        hideLoadingDialog()

        // Load fail → bỏ qua quảng cáo nếu đang chờ (tự động hoặc user nhấn)
        if (pendingAutoShow || hasUserClickedNext) {
            goToNextLevel()
        }
    }

    override fun onAdFailedToShow(error: String) {
        Log.e(TAG, "onAdFailedToShow: $error")
        hideLoadingDialog()
        goToNextLevel()
    }

    override fun onUserEarnedReward(item: RewardItem) {}

    private fun goToNextLevel() {
        Log.d(TAG, "goToNextLevel() → Chuyển sang màn tiếp theo")
        hasUserClickedNext = false
        pendingAutoShow = false
        wasPausedByChildActivity = false

        startActivity(Intent(this, MainAdsActivity::class.java))
        // Không finish() để back lại vẫn hoạt động đúng
    }

    private fun showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = Dialog(this).apply {
                setContentView(ProgressBar(this@MainInterstitialAdActivity))
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                setCancelable(false)
            }
        }
        progressDialog?.show()
        Log.d(TAG, "showLoadingDialog()")
    }

    private fun hideLoadingDialog() {
        progressDialog?.dismiss()
        Log.d(TAG, "hideLoadingDialog()")
    }

    private fun updateButtonState(state: ButtonState) {
        binding.btNext.isEnabled = (state == ButtonState.READY)
        binding.btNext.text = if (state == ButtonState.READY) "Tiếp tục" else "Đang tải..."
        Log.d(TAG, "updateButtonState: $state")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        adManager.destroy()
        super.onDestroy()
    }
}