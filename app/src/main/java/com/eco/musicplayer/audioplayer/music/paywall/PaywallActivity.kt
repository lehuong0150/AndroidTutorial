package com.eco.musicplayer.audioplayer.music.paywall

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallBinding
import com.eco.musicplayer.audioplayer.music.extension.applyFigmaTextStyle

class PaywallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        // 1. Áp dụng cho "30%" (Kích thước 100%, Shadow Y=3)
        binding.textPercent.applyFigmaTextStyle(
            colorStartHex = "#F3F3FC",
            colorEndHex = "#A2B1DA",
            shadowOffsetY = 3.0f,
            targetScale = 1.0f, // Kích thước cơ bản
            context = this
        )

        // 2. Áp dụng cho "OFF" (Kích thước 72%, Shadow Y=2)
        binding.textOff.applyFigmaTextStyle(
            colorStartHex = "#EEEEF2",
            colorEndHex = "#C8D0E7",
            shadowOffsetY = 2.0f,
            targetScale = 0.72f, // 72px / 100px
            context = this
        )

        binding.textPercent.text = getString(R.string.dialog_weekly_30,30)
    }

    private fun setupClickListeners() = with(binding) {

        btnPayWall1.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallSaleActivity::class.java))
        }

        btnPayWall2.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, DialogWeeklyActivity::class.java))
        }

        btnPayWall3.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, DialogYearlyActivity::class.java))
        }

        btnPayWall4.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallOnboardingActivity::class.java))
        }

        btnPayWall5.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, UnlockActivity::class.java))
        }

        btnPayWall6.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallBottomSheetActivity::class.java))
        }

        btnPayWall7.setOnClickListener {
            startActivity(Intent(this@PaywallActivity, PaywallFullActivity::class.java))
        }
    }
}

