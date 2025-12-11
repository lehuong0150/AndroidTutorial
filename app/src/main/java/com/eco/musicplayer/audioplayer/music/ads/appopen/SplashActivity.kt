package com.eco.musicplayer.audioplayer.music.ads.appopen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.ads.reward.MainRewardAdActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private var hasNavigated = false
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (!hasNavigated) {
            Log.d(TAG, "Timeout (5s) - navigating to MainActivityaa")
            navigateToMain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val app = application as? MyApplication
        if (app != null) {
            app.getAppOpenAdManager().loadAdWithCallback(object : AdLoadCallback {
                override fun onAdLoaded() {
                    Log.d(TAG, "Ad loaded successfully")
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    navigateToMain()
                }

                override fun onAdFailedToLoad() {
                    Log.d(TAG, "Ad loaded Fail")
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    navigateToMain()
                }
            })
            timeoutHandler.postDelayed(timeoutRunnable, 10000)
        } else {
            Log.e(TAG, "MyApplication not found - navigating to MainActivity")
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true
        Log.d(TAG, "Moving to MainRewardAdActivity")
        val intent = Intent(this, MainRewardAdActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeoutHandler.removeCallbacks(timeoutRunnable)
        Log.d(TAG, "Splash destroyed")
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}