package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.databinding.ActivityPaywallSaleBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallSaleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallSaleBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallSaleBinding.inflate(layoutInflater)
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
        setupBottomSheet()
        setupClickListeners()
        showLoading()

        binding.root.postDelayed({
            val isSuccess = false
            if (isSuccess) showSuccess() else showFailed()
        }, 5000)
    }

    private fun setupBottomSheet() {
        binding.layoutContent.let {
            bottomSheetBehavior = BottomSheetBehavior.from(it).apply {
                isHideable = false
                state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        layoutContent.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

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
        groupContent.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        groupLoading.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        groupLoading.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showFailed() = with(binding) {
        groupLoading.visibility = View.INVISIBLE
        groupContent.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onBackPressed() {
        bottomSheetBehavior.run {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                super.onBackPressed()
            }
        }
    }
}
