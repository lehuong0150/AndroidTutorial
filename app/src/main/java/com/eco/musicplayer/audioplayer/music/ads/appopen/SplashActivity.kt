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
import com.eco.musicplayer.audioplayer.music.ads.MainAdsActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "Splash started - Waiting for ad to load...")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Moving to MainAdsActivity")
            val intent = Intent(this, MainAdsActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Splash destroyed")
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}