package com.eco.musicplayer.audioplayer.music.paywall

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.billing.BillingListener
import com.eco.musicplayer.audioplayer.music.billing.BillingManager
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallBottomSheetBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallBottomSheetActivity : AppCompatActivity(), BillingListener {

    companion object {
        private const val TAG = "PaywallBottomSheet"

        // Default values
        private const val DEFAULT_SUBSCRIPTION_ID = "free_123"
        private const val DEFAULT_LIFETIME_ID = "test3"
    }

    private lateinit var binding: ActivityPaywallBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var billingManager: BillingManager

    // Nhận dữ liệu từ Intent - Hỗ trợ nhiều subscriptions
    private var subscriptionIds = listOf<String>()
    private var subscriptionOfferIds = listOf<String>()
    private var lifetimeId = DEFAULT_LIFETIME_ID
    private var selectionPosition = 1

    private var isLifetimeSelected = true
    private var hasTriedFreeTrial = false

    private var weeklyPrice = ""
    private var lifetimePrice = ""
    private var weeklyOffer: OfferInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaywallBottomSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.txtPolicy) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        getDataFromIntent()
        setupBillingManager()
        setupBottomSheet()
        setupClickListeners()
        setupInitialState()
    }

    private fun getDataFromIntent() {
        intent?.let {
            subscriptionIds = it.getStringArrayListExtra(PaywallActivity.EXTRA_SUBSCRIPTION_ID)
                ?: listOf(DEFAULT_SUBSCRIPTION_ID)

            subscriptionOfferIds =
                it.getStringArrayListExtra(PaywallActivity.EXTRA_SUBSCRIPTION_OFFER_ID)
                    ?: listOf()

            lifetimeId = it.getStringExtra(PaywallActivity.EXTRA_LIFETIME_ID)
                ?: DEFAULT_LIFETIME_ID

            selectionPosition = it.getIntExtra(PaywallActivity.EXTRA_SELECTION_POSITION, 1)

            Log.d(TAG, "===== Received Data =====")
            Log.d(TAG, "Subscription IDs: $subscriptionIds")
            Log.d(TAG, "Offer IDs: $subscriptionOfferIds")
            Log.d(TAG, "Lifetime ID: $lifetimeId")
            Log.d(TAG, "Selection Position: $selectionPosition")
            Log.d(TAG, "========================")

            isLifetimeSelected = (selectionPosition == 1)
        }
    }

    private fun setupBillingManager() {
        billingManager = BillingManager(
            context = this,
            listener = this,
            subscriptionIds = subscriptionIds,
            lifetimeId = lifetimeId
        )
        billingManager.initialize()
    }

    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutPwBottomSheet

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheetView.post {
                val screenHeight = resources.displayMetrics.heightPixels
                val percentage = 0.68f
                peekHeight = (screenHeight * percentage).toInt()
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            layoutPwBottomSheet.setOnClickListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            layoutPwYearly.setOnClickListener {
                if (!isLifetimeSelected) {
                    isLifetimeSelected = true
                    updateUI()
                }
            }

            layoutPwWeekly.setOnClickListener {
                if (isLifetimeSelected) {
                    isLifetimeSelected = false
                    updateUI()
                }
            }

            btnTryTree.setOnClickListener {
                handlePurchaseClick()
            }

            btnClose.setOnClickListener {
                finish()
            }
        }
    }

    private fun handlePurchaseClick() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val hasTrial = !isLifetimeSelected && trialDays > 0

        when {
            isLifetimeSelected -> {
                Log.d(TAG, "Purchase Lifetime (no trial)")
                launchPurchase()
            }

            hasTrial && !hasTriedFreeTrial -> {
                Log.d(TAG, "Use Free Trial ($trialDays days)")
                hasTriedFreeTrial = true
                updateUI()
            }

            else -> {
                Log.d(TAG, "Purchase Weekly (continue)")
                launchPurchase()
            }
        }
    }

    private fun launchPurchase() {
        val productId: String
        val offerId: String

        if (isLifetimeSelected) {
            productId = lifetimeId
            offerId = ""
        } else {
            productId = subscriptionIds.firstOrNull() ?: DEFAULT_SUBSCRIPTION_ID

            offerId = weeklyOffer?.offerId?.takeIf { it.isNotEmpty() }
                ?: subscriptionOfferIds.firstOrNull()
                        ?: ""
        }

        Log.d(TAG, "Launching purchase: Product=$productId, Offer=$offerId")

        showPurchaseLoading()
        billingManager.launchPurchaseFlow(
            activity = this,
            productId = productId,
            offerId = offerId,
            lifetimeId = lifetimeId
        )
    }

    private fun setupInitialState() {
        showLoading()
    }

    private fun showLoading() {
        with(binding) {
            pgbLoadInfo.visibility = View.VISIBLE
            pgbLoadWeekly.visibility = View.VISIBLE
            pgbLoadYearly.visibility = View.VISIBLE

            groupWeekly.visibility = View.INVISIBLE
            groupYearly.visibility = View.INVISIBLE
            groupContent.visibility = View.INVISIBLE

            btnTryTree.isEnabled = false
            btnTryTree.text = ""
            btnTryTree.setBackgroundResource(R.drawable.bg_pw_loading)
            txtPwYearlyPlan.text = getString(R.string.paywall_lifetime_title)
        }
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

    private fun showSuccess() {
        with(binding) {
            pgbLoadInfo.visibility = View.INVISIBLE
            pgbLoadWeekly.visibility = View.INVISIBLE
            pgbLoadYearly.visibility = View.INVISIBLE

            groupWeekly.visibility = View.VISIBLE
            groupYearly.visibility = View.VISIBLE
            groupContent.visibility = View.VISIBLE

            btnTryTree.isEnabled = true
            btnTryTree.setBackgroundResource(R.drawable.btn_pw_bottom_sheet_free)
            txtPwYearly.text = ""
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
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_selected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_unselected)
            } else {
                txtPwYearlySaved.setBackgroundResource(R.drawable.bg_pw_yearly_title_unselected)
                layoutPwYearly.setBackgroundResource(R.drawable.bg_pw_yearly_unselected)
                layoutPwWeekly.setBackgroundResource(R.drawable.bg_pw_selected)
            }
        }
    }

    private fun updateButtonText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        val hasTrial = !isLifetimeSelected && trialDays > 0

        binding.btnTryTree.text = when {
            isLifetimeSelected -> getString(R.string.paywall_full_btn_continue)
            hasTrial && !hasTriedFreeTrial -> getString(R.string.paywall_bottom_sheet_btn_free)
            else -> getString(R.string.paywall_full_btn_continue)
        }
    }

    private fun updateFreeTrialText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        binding.txtPwFreeTrial.text = when {
            isLifetimeSelected -> getString(R.string.paywall_lifetime_label)
            !hasTriedFreeTrial && trialDays > 0 -> getString(
                R.string.paywall_bottom_sheet_free_trial,
                trialDays
            )

            else -> getString(R.string.paywall_bottom_sheet_cancel)
        }
    }

    private fun updateContentText() {
        val trialDays = weeklyOffer?.freeTrialDays ?: 0
        binding.txtPwContent.text = when {
            isLifetimeSelected ->
                getString(R.string.paywall_lifetime_content_yearly, lifetimePrice)

            trialDays > 0 && !hasTriedFreeTrial ->
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
                "Khong the ket noi Google Play. Vui long thu lai.",
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
        val targetOfferId = subscriptionOfferIds.firstOrNull() ?: ""

        val offerLoad = if (targetOfferId.isNotEmpty()) {
            billingManager.getOfferByOfferId(targetOfferId)
        } else {
            null
        } ?: billingManager.getOfferByTrialDays(3)
        ?: weeklyOffer

        this.weeklyOffer = offerLoad
        this.weeklyPrice = offerLoad?.formattedPrice ?: weeklyPrice
        this.lifetimePrice = lifetimePrice

        if (weeklyPrice.isEmpty() && lifetimePrice.isEmpty()) {
            Toast.makeText(
                this,
                "Khong tai duoc thong tin gia tu Google Play",
                Toast.LENGTH_SHORT
            ).show()
        }

        runOnUiThread {
            showSuccess()
        }
    }

    override fun onPurchaseSuccess() {
        Log.d(TAG, "Purchase successful")
        hidePurchaseLoading()
        Toast.makeText(this, "Thanh toan thanh cong!", Toast.LENGTH_SHORT).show()

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