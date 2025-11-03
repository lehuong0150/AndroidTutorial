package com.eco.musicplayer.audioplayer.music.paywall

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.billingManager.BillingListener
import com.eco.musicplayer.audioplayer.music.billingManager.BillingManager
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallOnboardingBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo

class PaywallOnboardingActivity : FullscreenActivity(), BillingListener {

    companion object {
        private const val TAG = "PaywallOnboarding"

        // Product IDs
        private const val SUBSCRIPTION_ID = "free_123"
        private const val LIFETIME_ID = "test3"

        // Offer IDs
        private const val OFFER_3_DAYS = "3days"
        private const val OFFER_7_DAYS = "7days"
    }

    private lateinit var binding: ActivityPaywallOnboardingBinding
    private lateinit var billingManager: BillingManager

    private var isLifetimeSelected = true
    private var hasUserAlreadyUsedTrial = false  // Trạng thái từ Google Play
    private var hasClickedTrialButton = false     // User đã click switch trial

    private var weeklyPrice = ""
    private var lifetimePrice = ""
    private var weeklyOffer: OfferInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaywallOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupBillingManager()
        setupClickListeners()
        setupInitialState()
    }

    private fun applyWindowInsets() {
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
    }

    private fun setupBillingManager() {
        billingManager = BillingManager(
            context = this,
            listener = this,
            subscriptionIds = listOf(SUBSCRIPTION_ID),
            lifetimeId = LIFETIME_ID
        )
        billingManager.initialize()
    }

    private fun setupInitialState() {
        showLoading()
    }

    private fun setupClickListeners() = with(binding) {
        radioGroupPlan.setOnCheckedChangeListener { _, checkedId ->
            handlePlanSelection(checkedId)
        }

        swFree.setOnCheckedChangeListener { _, isChecked ->
            handleTrialSwitch(isChecked)
        }

        btnTryTree.setOnClickListener {
            handlePurchaseClick()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun handlePlanSelection(checkedId: Int) {
        isLifetimeSelected = (checkedId == R.id.rbYearly)

        // Reset trial button state khi đổi plan
        if (isLifetimeSelected) {
            hasClickedTrialButton = false
        }

        updateUI()
    }

    private fun handleTrialSwitch(isChecked: Boolean) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canUseTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        if (isChecked && canUseTrial) {
            hasClickedTrialButton = true
        } else {
            hasClickedTrialButton = false
        }

        updateUI()
    }

    private fun handlePurchaseClick() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canShowTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial &&
                hasClickedTrialButton

        when {
            isLifetimeSelected -> {
                Log.d(TAG, "Purchase Lifetime (no trial)")
                launchPurchase()
            }

            canShowTrial -> {
                Log.d(TAG, "User confirmed trial via switch - showing trial offer ($trialDays days)")
                launchPurchase()
            }

            else -> {
                Log.d(TAG, "Purchase Weekly (direct payment)")
                launchPurchase()
            }
        }
    }

    private fun launchPurchase() {
        val productId = if (isLifetimeSelected) {
            LIFETIME_ID
        } else {
            SUBSCRIPTION_ID
        }

        val offerId = if (isLifetimeSelected) {
            ""
        } else {
            weeklyOffer?.offerId ?: ""
        }

        Log.d(TAG, "Launching purchase: productId=$productId, offerId=$offerId, hasClickedTrial=$hasClickedTrialButton")

        showPurchaseLoading()
        billingManager.launchPurchaseFlow(
            activity = this,
            productId = productId,
            offerId = offerId,
            lifetimeId = LIFETIME_ID
        )
    }

    private fun updateUI() {
        updateContentText()
        updateFreeTrialText()
        updateButtonText()
        updateSwitchVisibility()
    }

    private fun updateContentText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canUseTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        txtContent.text = when {
            isLifetimeSelected ->
                getString(R.string.paywall_onboarding_content_lifetime, lifetimePrice)

            canUseTrial && hasClickedTrialButton ->
                getString(R.string.paywall_onboarding_content_weekly, weeklyPrice, trialDays)

            else ->
                getString(R.string.paywall_onboarding_content_weekly_direct, weeklyPrice)
        }
    }

    private fun updateButtonText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canUseTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        btnTryTree.text = when {
            isLifetimeSelected -> getString(R.string.paywall_btn_free_trial_continue)
            canUseTrial && hasClickedTrialButton -> getString(R.string.paywall_onboarding_btn_free)
            else -> getString(R.string.paywall_btn_free_trial_continue)
        }
    }

    private fun updateFreeTrialText() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canUseTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        txtFreeTrial.text = when {
            isLifetimeSelected -> getString(R.string.paywall_free_trial_disabled)
            !canUseTrial -> getString(R.string.paywall_free_trial_disabled)
            hasClickedTrialButton -> getString(R.string.paywall_free_trial_enabled)
            else -> getString(R.string.paywall_free_trial_disabled)
        }
    }

    private fun updateSwitchVisibility() = with(binding) {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canUseTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        layoutFreeTrial.visibility = if (canUseTrial) View.VISIBLE else View.GONE

        if (canUseTrial) {
            // Chỉ update switch khi không đang được user tương tác
            if (!swFree.isPressed) {
                swFree.isChecked = hasClickedTrialButton
            }
        }
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        txtContent.visibility = View.VISIBLE
        rbYearly.text = getString(R.string.paywall_lifetime_title)
        btnTryTree.apply {
            isEnabled = false
            setBackgroundResource(R.drawable.bg_pw_loading)
            text = null
        }
        layoutFreeTrial.visibility = View.GONE
    }

    private fun showPurchaseLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        btnTryTree.apply {
            isEnabled = false
            text = null
        }
    }

    private fun hidePurchaseLoading() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnTryTree.isEnabled = true
        updateButtonText()
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        txtContent.visibility = View.VISIBLE
        btnTryTree.apply {
            isEnabled = true
            setBackgroundResource(R.drawable.bg_pw_btn_free)
        }
        updateUI()
    }

    // ============ BillingListener Callbacks ============

    override fun onBillingSetupFinished(isSuccess: Boolean) {
        if (!isSuccess) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Không thể kết nối Google Play. Vui lòng thử lại.",
                    Toast.LENGTH_LONG
                ).show()
                showLoading()
            }
        } else {
            Log.d(TAG, "Billing setup success, waiting for products...")
        }
    }

    override fun onProductDetailsLoaded(
        weeklyPrice: String,
        lifetimePrice: String,
        weeklyOffer: OfferInfo?
    ) {
        // Tìm offer 3 days hoặc 7 days
        val offer3Days = billingManager.getOfferByOfferId(OFFER_3_DAYS)
            ?: billingManager.getOfferByTrialDays(3)
            ?: billingManager.getOfferByOfferId(OFFER_7_DAYS)
            ?: billingManager.getOfferByTrialDays(7)
            ?: weeklyOffer

        this.weeklyOffer = offer3Days
        this.weeklyPrice = offer3Days?.formattedPrice ?: weeklyPrice
        this.lifetimePrice = lifetimePrice

        Log.d(TAG, """
            Products loaded:
              Weekly Offer: ${offer3Days?.offerId}
              Weekly Price: ${this.weeklyPrice}
              Lifetime Price: $lifetimePrice
              Free Trial: ${offer3Days?.freeTrialDays} days
        """.trimIndent())

        if (this.weeklyPrice.isEmpty() && lifetimePrice.isEmpty()) {
            Toast.makeText(
                this,
                "Không tải được thông tin giá từ Google Play",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Check trial eligibility sau khi load product details
        billingManager.checkTrialEligibility(SUBSCRIPTION_ID)

        runOnUiThread {
            showSuccess()
        }
    }

    override fun checkTrialEligibility(hasUsedTrial: Boolean) {
        Log.d(TAG, "Trial eligibility checked: hasUsedTrial = $hasUsedTrial")
        hasUserAlreadyUsedTrial = hasUsedTrial

        runOnUiThread {
            updateUI()
        }
    }

    override fun onPurchaseSuccess() {
        Log.d(TAG, "Purchase successful")
        hidePurchaseLoading()
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }

    override fun onPurchaseFailed(errorMessage: String) {
        Log.e(TAG, "Purchase failed: $errorMessage")
        hidePurchaseLoading()
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}