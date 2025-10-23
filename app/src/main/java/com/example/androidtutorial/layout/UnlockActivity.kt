package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
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
            val isSuccess = true
            if (isSuccess) showSuccess()
        }, 2000)

        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        btnTryTree.setOnClickListener {
            // handleClaimOffer()
        }
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        btnTryTree.isEnabled = false
        btnTryTree.text = ""
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnTryTree.isEnabled = true
        btnTryTree.text = getString(R.string.paywall_onboarding_btn_free)
    }
}
