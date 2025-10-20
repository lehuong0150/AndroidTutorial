package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityPaywallSaleBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallSaleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaywallSaleBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomSheet()
        setupClickListeners()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutContent).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupClickListeners() {
        binding.layoutContent.setOnClickListener {
            if (binding.txtPrice.visibility != View.VISIBLE) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.txtTryAgain.setOnClickListener {
            // loadBillingData()
        }

        // Button Claim Offer
        binding.btnClaimOffer.setOnClickListener {
            //handleClaimOffer()
        }
    }

    private fun showSuccess() {

        binding.btnClaimOffer.visibility = View.VISIBLE
        binding.txtContent.visibility = View.VISIBLE
        binding.txtPolicy.visibility = View.VISIBLE
        binding.layoutLoadFail.visibility = View.INVISIBLE

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showFailed() {

        binding.btnClaimOffer.visibility = View.INVISIBLE
        binding.txtContent.visibility = View.INVISIBLE

        binding.layoutLoadFail.visibility = View.VISIBLE

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }
}