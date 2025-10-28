package com.eco.musicplayer.audioplayer.music.paywall

import android.graphics.Paint
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
import com.eco.musicplayer.audioplayer.music.databinding.ActivityPaywallSaleBinding
import com.eco.musicplayer.audioplayer.music.models.OfferInfo
import com.eco.musicplayer.audioplayer.music.paywall.PaywallBottomSheetActivity.Companion
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay

class PaywallSaleActivity : AppCompatActivity(), BillingListener {
    companion object {
        private const val TAG = "PaywallSaleActivity"
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
        billingManager = BillingManager(this, this)
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

        topLayout?.let {
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        txtTryAgain.setOnClickListener {
            showLoading()
            root.postDelayed({
                showSuccess()
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
        pgbLoadInfo.visibility = View.VISIBLE
        btnClaimOffer.isEnabled = false
        btnClaimOffer.text = ""
        btnClaimOffer.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun showSuccess() = with(binding) {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        pgbLoadInfo.visibility = View.INVISIBLE
        layoutLoadFail.visibility = View.INVISIBLE
        groupContent.visibility = View.VISIBLE
        btnClaimOffer.visibility = View.VISIBLE
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
            if (weeklyPrice.isNotEmpty()) {
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
                Log.d(TAG, "weeklyPrice null")
            }
        }
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
        val offerIntroPrice = billingManager.getOfferByOfferId(BillingManager.OFFER_INTRO_PRICE)
        this.weeklyOffer = offerIntroPrice
        this.weeklyPrice = offerIntroPrice?.formattedPrice ?: ""
        this.weeklyIntroPrice = offerIntroPrice?.introPrice ?: ""
        offerIntroPrice?.let { offer ->
            introPeriod = if (offer.introPriceCycle > 0)
                offer.introPriceCycle.toString()
            else
                offer.introPriceDays.toString()
        }

        if (this.weeklyPrice.isEmpty() && lifetimePrice.isEmpty()) {
            Toast.makeText(this, "Không tải được thông tin giá từ Google Play", Toast.LENGTH_SHORT)
                .show()
        } else {
            Log.d(TAG, "Prices loaded successfully")
        }

        runOnUiThread {
            showSuccess()
        }
    }

    override fun onPurchaseSuccess() {}

    override fun onPurchaseFailed(errorMessage: String) {}

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
