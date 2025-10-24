package com.eco.musicplayer.audioplayer.music.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityDialogWeeklyBinding

class DialogWeeklyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialogWeeklyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogWeeklyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        showLoading()

        binding.root.postDelayed({
            val isSuccess = false
            if (isSuccess) showSuccess() else showFailed()
        }, 2000)

        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        txtTryAgain.setOnClickListener {
            showLoading()
            root.postDelayed({
                showSuccess() // hoặc showFailed()
            }, 2000)
        }

        btnClaimOffer.setOnClickListener {
            // handleClaimOffer()
        }
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun showLoading() = with(binding) {
        layoutLoadFail.visibility = View.INVISIBLE
        btnClaimOffer.isEnabled = false
        btnClaimOffer.text= ""
        pgbLoadInfo.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnClaimOffer.visibility = View.VISIBLE
        btnClaimOffer.text= getString(R.string.dialog_weekly_btn_offer)
        layoutLoadFail.visibility = View.INVISIBLE
    }

    private fun showFailed() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnClaimOffer.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.VISIBLE
    }
}
