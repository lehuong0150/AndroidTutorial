package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.databinding.ActivityDialogWeeklyBinding

class DialogWeeklyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialogWeeklyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogWeeklyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading()

        binding.root.postDelayed({
            val isSuccess = false
            if (isSuccess) showSuccess() else showFailed()
        }, 5000)

        setupClickListeners()
    }

    private fun setupClickListeners() = with(binding) {
        txtTryAgain.setOnClickListener {
            showLoading()
            root.postDelayed({
                showSuccess() // hoặc showFailed()
            }, 5000)
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
        groupContent.visibility = View.INVISIBLE
        pgbLoadInfo.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
    }

    private fun showFailed() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.VISIBLE
    }
}
