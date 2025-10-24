package com.eco.musicplayer.audioplayer.music.layout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallBinding

class PaywallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
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
