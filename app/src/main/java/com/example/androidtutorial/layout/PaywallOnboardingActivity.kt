package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityPaywallOnboardingBinding

class PaywallOnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaywallOnboardingBinding
    private var isYearlySelected = true
    private var isFreeTrialEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallOnboardingBinding.inflate(layoutInflater)
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
        setupInitialState()
        setupListeners()
    }

    private fun setupInitialState() {
        showLoading()

        binding.root.postDelayed({
            showSuccess()
        }, 2000)
    }

    private fun setupListeners() = with(binding) {
        radioGroupPlan.setOnCheckedChangeListener { _, checkedId ->
            isYearlySelected = (checkedId == R.id.rbYearly)
            updateUI()
        }

        swFree.setOnCheckedChangeListener { _, isChecked ->
            isFreeTrialEnabled = isChecked
            updateUI()
        }

        btnTryTree.setOnClickListener {
            updateButtonText()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun updateUI() {
        updateContentText()
        updateFreeTrialText()
    }

    private fun updateContentText() = with(binding) {
        txtContent.text = when {
            isYearlySelected && isFreeTrialEnabled ->
                getString(R.string.paywall_onboarding_content_yearly)

            isYearlySelected && !isFreeTrialEnabled ->
                getString(R.string.paywall_onboarding_content_yearly_direct)

            !isYearlySelected && isFreeTrialEnabled ->
                getString(R.string.paywall_onboarding_content_weekly)

            else ->
                getString(R.string.paywall_onboarding_content_weekly_direct)
        }
    }

    private fun updateButtonText() = with(binding) {
        btnTryTree.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_btn_free_trial_continue)
        } else {
            getString(R.string.paywall_onboarding_btn_free)
        }
    }

    private fun updateFreeTrialText() = with(binding) {
        txtFreeTrial.text = if (isFreeTrialEnabled) {
            getString(R.string.paywall_free_trial_enabled)
        } else {
            getString(R.string.paywall_free_trial_disabled)
        }
    }

    private fun processPurchase() {
        finish()
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        groupContent.visibility = View.VISIBLE
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        updateUI()
    }
}
