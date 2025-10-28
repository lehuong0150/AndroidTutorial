package com.eco.musicplayer.audioplayer.music.paywall

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.billing.BillingListener
import com.eco.musicplayer.audioplayer.music.billing.BillingManager
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallOnboardingBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo


class PaywallOnboardingActivity : AppCompatActivity(), BillingListener {

    companion object {
        private const val TAG = "PaywallOnboardingActivity"
    }

    private lateinit var binding: ActivityPaywallOnboardingBinding
    private lateinit var billingManager: BillingManager

    private var isLifetimeSelected = true
    private var hasTriedFreeTrial = false

    private var weeklyPrice = ""
    private var lifetimePrice = ""

    private var weeklyOffer: OfferInfo? = null

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
        setupBillingManager()
        setupClickListeners()
        setupInitialState()
    }

    private fun setupBillingManager() {
        billingManager = BillingManager(this, this)
        billingManager.initialize()
    }

    private fun setupInitialState() {
        showLoading()
    }

    private fun setupClickListeners() = with(binding) {
        radioGroupPlan.setOnCheckedChangeListener { _, checkedId ->
            isLifetimeSelected = (checkedId == R.id.rbYearly)
            updateUI()
        }

        swFree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!isLifetimeSelected && (weeklyOffer?.freeTrialDays
                        ?: 0) > 0 && !hasTriedFreeTrial
                ) {
                    hasTriedFreeTrial = true
                }
            } else {
                hasTriedFreeTrial = false
            }
            updateUI()
        }

        btnTryTree.setOnClickListener {
            handlePurchaseClick()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun handlePurchaseClick() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val hasTrial = !isLifetimeSelected && trialDays > 0

        when {
            isLifetimeSelected -> {
                Log.d(TAG, "Purchase Lifetime (no trial)")
                launchPurchase(useFreeTrial = false)
            }

            hasTrial && !hasTriedFreeTrial -> {
                Log.d(TAG, "Use Free Trial ($trialDays days)")
                hasTriedFreeTrial = true
                updateUI()
            }

            else -> {
                Log.d(TAG, "Purchase Weekly (continue)")
                launchPurchase(useFreeTrial = hasTriedFreeTrial)
            }
        }
    }

    private fun launchPurchase(useFreeTrial: Boolean) {
        val selectedOfferId = if (isLifetimeSelected) {
            ""
        } else {
            weeklyOffer?.offerId ?: ""
        }

        Log.d(
            TAG,
            "Launch Purchase: isLifetime=$isLifetimeSelected, offerId=$selectedOfferId, useTrial=$useFreeTrial"
        )
        showPurchaseLoading()
        billingManager.launchPurchaseFlow(this, isLifetimeSelected, selectedOfferId)
    }

    private fun updateUI() {
        updateContentText()
        updateFreeTrialText()
        updateButtonText()
        updateSwitchVisibility()
    }

    private fun updateContentText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0

        txtContent.text = when {
            isLifetimeSelected ->
                getString(R.string.paywall_onboarding_content_lifetime, lifetimePrice)

            trialDays > 0 && !hasTriedFreeTrial ->
                getString(
                    R.string.paywall_onboarding_content_weekly_direct,
                    weeklyPrice
                )

            !isLifetimeSelected && hasTriedFreeTrial ->
                getString(R.string.paywall_onboarding_content_weekly, weeklyPrice, trialDays)

            else ->
                getString(R.string.paywall_onboarding_content_weekly_direct)
        }
    }

    private fun updateButtonText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val hasTrial = !isLifetimeSelected && trialDays > 0

        btnTryTree.text = when {
            isLifetimeSelected -> getString(R.string.paywall_btn_free_trial_continue)

            hasTrial && hasTriedFreeTrial -> getString(R.string.paywall_onboarding_btn_free)

            else -> getString(R.string.paywall_btn_free_trial_continue)
        }
    }

    private fun updateFreeTrialText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0

        txtFreeTrial.text = when {
            isLifetimeSelected -> getString(R.string.paywall_free_trial_disabled)

            !hasTriedFreeTrial && trialDays > 0 -> {
                getString(R.string.paywall_free_trial_disabled)
            }

            else -> getString(R.string.paywall_free_trial_enabled)
        }
    }

    private fun updateSwitchVisibility() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val shouldShowSwitch = !isLifetimeSelected && trialDays > 0

        if (shouldShowSwitch && layoutFreeTrial.visibility != View.VISIBLE) {
            layoutFreeTrial.visibility = View.VISIBLE
        } else if (!shouldShowSwitch && layoutFreeTrial.visibility != View.GONE) {
            layoutFreeTrial.visibility = View.GONE
        }
        if (shouldShowSwitch) {
            swFree.isChecked = hasTriedFreeTrial
        }
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        txtContent.visibility = View.VISIBLE
        rbYearly.text = getString(R.string.paywall_lifetime_title)
        btnTryTree.isEnabled = false
        btnTryTree.setBackgroundResource(R.drawable.bg_pw_loading)
        btnTryTree.text = ""
        layoutFreeTrial.visibility = View.GONE
    }

    private fun showPurchaseLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            btnTryTree.isEnabled = false
            btnTryTree.text = ""
        }
    }

    private fun hidePurchaseLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            btnTryTree.isEnabled = true
            updateButtonText()
        }
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        txtContent.visibility = View.VISIBLE
        btnTryTree.isEnabled = true
        btnTryTree.setBackgroundResource(R.drawable.bg_pw_btn_free)
        updateUI()
    }

    override fun onBillingSetupFinished(isSuccess: Boolean) {
        if (!isSuccess) {
            Toast.makeText(
                this,
                "Không thể kết nối Google Play. Vui lòng thử lại.",
                Toast.LENGTH_LONG
            ).show()
            showSuccess()
        } else {
            Log.d(TAG, "Billing setup success, waiting for products...")
        }
    }

    override fun onProductDetailsLoaded(
        weeklyPrice: String,
        lifetimePrice: String,
        weeklyOffer: OfferInfo?
    ) {
        val offer3Days = billingManager.getOfferByOfferId(BillingManager.OFFER_3_DAYS)
            ?: billingManager.getOfferByTrialDays(3)

        this.weeklyOffer = offer3Days
        this.weeklyPrice = offer3Days?.formattedPrice ?: ""
        this.lifetimePrice = lifetimePrice

        if (weeklyPrice.isEmpty() && lifetimePrice.isEmpty()) {
            Toast.makeText(this, "Không tải được thông tin giá từ Google Play", Toast.LENGTH_SHORT)
                .show()
        } else {
            Log.d(TAG, "Prices loaded successfully")
        }

        runOnUiThread {
            showSuccess()
        }
    }

    override fun onPurchaseSuccess() {
        Log.d(TAG, "onPurchaseSuccess")
//        hidePurchaseLoading()
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }

    override fun onPurchaseFailed(errorMessage: String) {
        Log.e(TAG, "onPurchaseFailed: $errorMessage")
//        hidePurchaseLoading()
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}