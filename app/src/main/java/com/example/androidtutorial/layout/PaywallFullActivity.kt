package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityPaywallFullBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallFullActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaywallFullBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var isYearlySelected = true
    private var isFreeTrialEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaywallFullBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomSheet()
        setupInitialState()
        setupClickListeners()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutPwFull).apply {
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
        layoutPwFull.setOnClickListener {
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
            toggleFreeTrialState()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun toggleFreeTrialState() {
        isFreeTrialEnabled = !isFreeTrialEnabled
        updateFreeTrialUI()
    }

    private fun updateFreeTrialUI() = with(binding) {
        if (isFreeTrialEnabled) {
            // State: Try for free
            btnTryTree.text = getString(R.string.paywall_full_btn_free)
            txtPwFreeTrial.text = getString(R.string.paywall_full_free_trial)

            // Update content text
            updateContentTextWithTrial()
        } else {
            // State: Continue
            btnTryTree.text = getString(R.string.paywall_full_btn_continue)
            txtPwFreeTrial.text = getString(R.string.paywall_full_cancel)

            // Update content text
            updateContentTextWithoutTrial()
        }
    }

    private fun updateContentTextWithTrial() = with(binding) {
        txtPwContent.text = if (isYearlySelected) {
            getString(R.string.paywall_full_content_trial_yearly)
        } else {
            getString(R.string.paywall_full_content_trial_weekly)
        }
    }

    private fun updateContentTextWithoutTrial() = with(binding) {
        txtPwContent.text = if (isYearlySelected) {
            getString(R.string.paywall_full_content_yearly)
        } else {
            getString(R.string.paywall_full_content_weekly)
        }
    }

    private fun updatePlanSelection() {
        updatePlanBackground()
        updateContentText()
    }

    private fun updatePlanBackground() = with(binding) {
        if (isYearlySelected) {

            txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_selected)
            layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_selected)
            layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_unselected)
        } else {
            txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_unselected)
            layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_unselected)
            layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_selected)
        }
    }

    private fun updateContentText() = with(binding) {
        if (isFreeTrialEnabled) {
            updateContentTextWithTrial()
        } else {
            updateContentTextWithoutTrial()
        }
    }

    private fun processPurchase() {
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
        updateFreeTrialUI()
    }
}
