package com.eco.musicplayer.audioplayer.music.paywall

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.billingManager.BillingListener
import com.eco.musicplayer.audioplayer.music.billingManager.BillingManager
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallFullBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallFullActivity : FullscreenActivity(), BillingListener {

    companion object {
        private const val TAG = "PaywallFullActivity"

        private const val SUBSCRIPTION_ID = "free_123"
        private const val LIFETIME_ID = "test3"

        private const val OFFER_7_DAYS = "7days"
        private const val OFFER_3_DAYS = "3days"
    }

    private lateinit var binding: ActivityPaywallFullBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var billingManager: BillingManager

    private var isLifetimeSelected = true
    private var hasUserAlreadyUsedTrial = false  // Trạng thái từ Google Play
    private var hasClickedTrialButton = false

    private var weeklyPrice = ""
    private var lifetimePrice = ""
    private var weeklyOffer: OfferInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaywallFullBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupBillingManager()
        setupBottomSheet()
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

    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutPwFull
        val topLayout = binding.layoutGif

        val screenHeight = resources.displayMetrics.heightPixels
        val topRatio = 0.38f
        val bottomRatio = 0.69f

        topLayout.let {
            val params = it.layoutParams
            params.height = (screenHeight * topRatio).toInt()
            it.layoutParams = params
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheetView.post {
                peekHeight = (screenHeight * bottomRatio).toInt()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            layoutPwFull.setOnClickListener {
                expandBottomSheet()
            }

            layoutPwYearly.setOnClickListener {
                selectLifetimePlan()
            }

            layoutPwWeekly.setOnClickListener {
                selectWeeklyPlan()
            }

            btnTryTree.setOnClickListener {
                handlePurchaseClick()
            }

            btnClose.setOnClickListener {
                finish()
            }
        }
    }

    private fun expandBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun selectLifetimePlan() {
        if (!isLifetimeSelected) {
            isLifetimeSelected = true
            updateUI()
        }
    }

    private fun selectWeeklyPlan() {
        if (isLifetimeSelected) {
            isLifetimeSelected = false
            updateUI()
        }
    }

    private fun setupInitialState() {
        showLoading()
    }

    private fun handlePurchaseClick() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canShowTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial &&
                !hasClickedTrialButton

        when {
            isLifetimeSelected -> {
                Log.d(TAG, "Purchase Lifetime (no trial)")
                launchPurchase()
            }

            canShowTrial -> {
                Log.d(TAG, "User clicked 'Try Free' - showing trial offer ($trialDays days)")
                hasClickedTrialButton = true
                updateUI()
            }

            else -> {
                Log.d(TAG, "Purchase Weekly (continue)")
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

        Log.d(TAG, "Launching purchase: productId=$productId, offerId=$offerId")

        showPurchaseLoading()
        billingManager.launchPurchaseFlow(
            activity = this,
            productId = productId,
            offerId = offerId,
            lifetimeId = LIFETIME_ID
        )
    }

    private fun showPurchaseLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            btnTryTree.apply {
                isEnabled = false
                text = null
            }
        }
    }

    private fun hidePurchaseLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            btnTryTree.isEnabled = true
            updateButtonText()
        }
    }

    private fun showLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            pgbLoadWeekly.visibility = View.VISIBLE
            pgbLoadYearly.visibility = View.VISIBLE

            groupWeekly.visibility = View.INVISIBLE
            groupYearly.visibility = View.INVISIBLE
            groupContent.visibility = View.INVISIBLE

            btnTryTree.apply {
                isEnabled = false
                text = null
                setBackgroundResource(R.drawable.bg_pw_loading)
            }

            txtPwYearlyPlan.text = getString(R.string.paywall_lifetime_title)
        }
    }

    private fun showSuccess() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            pgbLoadWeekly.visibility = View.INVISIBLE
            pgbLoadYearly.visibility = View.INVISIBLE

            groupWeekly.visibility = View.VISIBLE
            groupYearly.visibility = View.VISIBLE
            groupContent.visibility = View.VISIBLE

            btnTryTree.apply {
                isEnabled = true
                setBackgroundResource(R.drawable.btn_pw_bottom_sheet_free)
            }
            txtPwYearly.text = null

            updateUI()
        }
    }

    private fun updateUI() {
        updatePlanBackground()
        updateButtonText()
        updateFreeTrialText()
        updateContentText()
        updatePriceDisplay()
    }

    private fun updatePlanBackground() {
        with(binding) {
            if (isLifetimeSelected) {
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_selected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_full_selected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_unselected)
            } else {
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_unselected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_yearly_unselected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_full_selected)
            }
        }
    }

    private fun updateButtonText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canShowTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial &&
                !hasClickedTrialButton

        binding.btnTryTree.text = when {
            isLifetimeSelected -> getString(R.string.paywall_full_btn_continue)
            canShowTrial -> getString(R.string.paywall_bottom_sheet_btn_free)
            else -> getString(R.string.paywall_full_btn_continue)
        }
    }

    private fun updateFreeTrialText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canShowTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        binding.txtPwFreeTrial.text = when {
            isLifetimeSelected -> getString(R.string.paywall_lifetime_label)
            canShowTrial && !hasClickedTrialButton -> getString(
                R.string.paywall_bottom_sheet_free_trial,
                trialDays
            )
            else -> getString(R.string.paywall_bottom_sheet_cancel)
        }
    }

    private fun updateContentText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val canShowTrial = !isLifetimeSelected &&
                trialDays > 0 &&
                !hasUserAlreadyUsedTrial

        binding.txtPwContent.text = when {
            isLifetimeSelected ->
                getString(R.string.paywall_lifetime_content_yearly, lifetimePrice)

            canShowTrial && !hasClickedTrialButton ->
                getString(R.string.paywall_bottom_sheet_content_weekly, weeklyPrice)

            else ->
                getString(R.string.paywall_bottom_sheet_content_trial_weekly, weeklyPrice)
        }
    }

    private fun updatePriceDisplay() {
        with(binding) {
            if (weeklyPrice.isNotEmpty()) {
                txtPwWeeklyPlanPrice.text = weeklyPrice
            } else {
                Log.w(TAG, "Weekly price is empty")
            }

            if (lifetimePrice.isNotEmpty()) {
                txtPwYearlyPlanPrice.text = lifetimePrice
                txtPwYearlyPlanContent.text = getString(R.string.paywall_lifetime_label)
            } else {
                Log.w(TAG, "Lifetime price is empty")
            }
        }
    }

    // ============ BillingListener Callbacks ============

    override fun onBillingSetupFinished(isSuccess: Boolean) {
        if (!isSuccess) {
            Toast.makeText(
                this,
                "Không thể kết nối Google Play. Vui lòng thử lại.",
                Toast.LENGTH_LONG
            ).show()
            showLoading()
        } else {
            Log.d(TAG, "Billing setup success, waiting for products...")
        }
    }

    override fun onProductDetailsLoaded(
        weeklyPrice: String,
        lifetimePrice: String,
        weeklyOffer: OfferInfo?
    ) {
        val offer7Days = billingManager.getOfferByOfferId(OFFER_7_DAYS)
            ?: billingManager.getOfferByTrialDays(7)
            ?: billingManager.getOfferByOfferId(OFFER_3_DAYS)
            ?: billingManager.getOfferByTrialDays(3)
            ?: weeklyOffer

        this.weeklyOffer = offer7Days
        this.weeklyPrice = offer7Days?.formattedPrice ?: weeklyPrice
        this.lifetimePrice = lifetimePrice

        Log.d(TAG, """
            Products loaded:
              Weekly Offer: ${offer7Days?.offerId}
              Weekly Price: ${this.weeklyPrice}
              Lifetime Price: $lifetimePrice
              Free Trial: ${offer7Days?.freeTrialDays} days
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