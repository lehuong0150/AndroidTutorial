package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityPaywallBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallBottomSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var isYearlySelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaywallBottomSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomSheet()
        setupInitialState()
        setupClickListeners()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutPwBottomSheet).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupInitialState() {
        showLoading()

        binding.root.postDelayed({
            showSuccess()
        }, 2000)
    }

    private fun setupClickListeners() = with(binding) {
        layoutPwBottomSheet.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        layoutPwYearly.setOnClickListener {
            isYearlySelected = true
            updatePlanSelection()
        }

        layoutPwWeekly.setOnClickListener {
            isYearlySelected = false
            updatePlanSelection()
        }

        btnTryTree.setOnClickListener {
            processPurchase()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun updatePlanSelection() {
        updatePlanBackground()
        updateContentText()
    }

    private fun updatePlanBackground() = with(binding) {
        layoutPwYearly.isSelected = isYearlySelected
        layoutPwWeekly.isSelected = !isYearlySelected
    }

    private fun updateContentText() = with(binding) {
        txtPwContent.text = if (isYearlySelected) {
            getString(R.string.paywall_bottom_sheet_content_yearly)
        } else {
            getString(R.string.paywall_bottom_sheet_content_weekly)
        }
    }

    private fun processPurchase() {
        // Handle purchase logic here
        finish()
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        groupContent.visibility = View.INVISIBLE
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        updatePlanSelection()
    }
}
