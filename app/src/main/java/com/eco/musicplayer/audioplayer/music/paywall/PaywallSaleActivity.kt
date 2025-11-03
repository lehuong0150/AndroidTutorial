package com.eco.musicplayer.audioplayer.music.paywall

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.billingManager.BillingListener
import com.eco.musicplayer.audioplayer.music.billingManager.BillingManager
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallSaleBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PaywallSaleActivity : FullscreenActivity(), BillingListener {

    companion object {
        private const val TAG = "PaywallSaleActivity"

        // Product IDs
        private const val SUBSCRIPTION_ID = "free_123"
        private const val LIFETIME_ID = "test3"

        // Offer IDs
        private const val OFFER_INTRO_PRICE = "intro-price"
        private const val OFFER_7_DAYS = "7days"
        private const val OFFER_3_DAYS = "3days"
    }

    private lateinit var binding: ActivityPaywallSaleBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var billingManager: BillingManager

    private var weeklyPrice = ""
    private var weeklyIntroPrice = ""
    private var introPeriod = ""
    private var weeklyOffer: OfferInfo? = null

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

        setupBillingManager()
        setupBottomSheet()
        setupClickListeners()
        setupInitialState()
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

    private fun setupBottomSheet() {
        val bottomSheetView = binding.layoutContent
        val topLayout = binding.layoutGif

        val screenHeight = resources.displayMetrics.heightPixels
        val topRatio = 0.68f
        val bottomRatio = 0.36f

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

    private fun setupClickListeners() = with(binding) {
        layoutContent.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        txtTryAgain.setOnClickListener {
            billingManager.initialize()
            showLoading()
        }

        btnClaimOffer.setOnClickListener {
            handleClaimOffer()
        }

        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun handleClaimOffer() {
        val offerId = weeklyOffer?.offerId ?: ""

        if (offerId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ưu đãi", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Claiming offer: productId=$SUBSCRIPTION_ID, offerId=$offerId")

        showPurchaseLoading()
        billingManager.launchPurchaseFlow(
            activity = this,
            productId = SUBSCRIPTION_ID,
            offerId = offerId
        )
    }

    private fun showLoading() = with(binding) {
        groupContent.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        pgbLoadInfo.visibility = View.VISIBLE
        btnClaimOffer.isEnabled = false
        btnClaimOffer.text = null
        btnClaimOffer.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showPurchaseLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        btnClaimOffer.isEnabled = false
        btnClaimOffer.text = null
    }

    private fun hidePurchaseLoading() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnClaimOffer.isEnabled = true
        btnClaimOffer.text = getString(R.string.paywall_sale_btn_offer)
    }

    private fun showSuccess() = with(binding) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        pgbLoadInfo.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        btnClaimOffer.visibility = View.VISIBLE
        btnClaimOffer.isEnabled = true
        btnClaimOffer.text = getString(R.string.paywall_sale_btn_offer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        updatePriceDisplay()
    }

    private fun showFailed() = with(binding) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        pgbLoadInfo.visibility = View.INVISIBLE
        groupContent.visibility = View.INVISIBLE
        btnClaimOffer.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun updatePriceDisplay() {
        val weeklyPriceNumber = weeklyPrice.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
        val introPriceNumber = weeklyIntroPrice.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0

        val offPercent = if (weeklyPriceNumber > 0) {
            ((weeklyPriceNumber - introPriceNumber) / weeklyPriceNumber * 100).toInt()
        } else {
            0
        }

        with(binding) {
            if (weeklyPrice.isNotEmpty() && weeklyIntroPrice.isNotEmpty()) {
                txtPrice.text = weeklyIntroPrice
                txtPriceOld.apply {
                    text = getString(R.string.paywall_sale_price_old, weeklyPrice)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                txtContent.text = getString(
                    R.string.paywall_sale_content,
                    weeklyIntroPrice,
                    introPeriod,
                    weeklyPrice
                )
                txtOff.text = getString(R.string.paywall_sale_off, offPercent)
            } else {
                Log.w(TAG, "Price information incomplete")
            }
        }
    }

    // ---------BillingListener callbacks ------------

    override fun onBillingSetupFinished(isSuccess: Boolean) {
        if (!isSuccess) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Không thể kết nối Google Play. Vui lòng thử lại.",
                    Toast.LENGTH_LONG
                ).show()
                showFailed()
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
        val offerIntroPrice = billingManager.getOfferByOfferId(OFFER_INTRO_PRICE)
            ?: billingManager.getOfferByOfferId(OFFER_7_DAYS)
            ?: billingManager.getOfferByOfferId(OFFER_3_DAYS)
            ?: weeklyOffer

        this.weeklyOffer = offerIntroPrice
        this.weeklyPrice = offerIntroPrice?.formattedPrice ?: ""
        this.weeklyIntroPrice = offerIntroPrice?.introPrice ?: ""

        offerIntroPrice?.let { offer ->
            introPeriod = when {
                offer.introPriceCycle > 0 -> offer.introPriceCycle.toString()
                offer.introPriceDays > 0 -> offer.introPriceDays.toString()
                else -> "1"
            }
        }

        Log.d(
            TAG, """
            Products loaded:
              Offer: ${offerIntroPrice?.offerId}
              Weekly Price: $weeklyPrice
              Intro Price: $weeklyIntroPrice
              Intro Period: $introPeriod
        """.trimIndent()
        )

        if (this.weeklyPrice.isEmpty() || this.weeklyIntroPrice.isEmpty()) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Không tải được thông tin giá từ Google Play",
                    Toast.LENGTH_SHORT
                ).show()
                showFailed()
            }
        } else {
            runOnUiThread {
                showSuccess()
            }
        }
    }

    override fun checkTrialEligibility(hasUsedTrial: Boolean) {
        TODO("Not yet implemented")
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

    override fun onBackPressed() {
        bottomSheetBehavior.run {
            if (state == BottomSheetBehavior.STATE_EXPANDED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}