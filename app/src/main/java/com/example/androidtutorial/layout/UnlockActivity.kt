package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading()

        binding.root.postDelayed({
            val isSuccess = true
            if (isSuccess) showSuccess()
        }, 5000)

        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        btnTryTree.setOnClickListener {
            // handleClaimOffer()
        }
    }

    private fun showLoading() = with(binding) {
        groupContent.visibility = View.INVISIBLE
        pgbLoadInfo.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
    }
}
