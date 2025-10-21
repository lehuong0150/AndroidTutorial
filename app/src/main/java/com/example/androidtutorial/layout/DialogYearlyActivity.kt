package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.databinding.ActivityDialogYearlyBinding

class DialogYearlyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDialogYearlyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogYearlyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoading()

        binding.root.postDelayed({
            val isSuccess = false
            if (isSuccess) showSuccess() else showFailed()
        }, 8000)

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
