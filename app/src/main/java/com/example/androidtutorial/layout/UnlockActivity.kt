package com.example.androidtutorial.layout

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivityUnlockBinding

    private lateinit var billingClient: BillingClient

    private var currentPurchase: Purchase? = null
    private var targetProductDetails: ProductDetails? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
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
        showLoading()

        binding.root.postDelayed({
            val isSuccess = true
            if (isSuccess) showSuccess()
        }, 2000)

        setupClickListeners()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object :BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {

                }
            }
        })
    }

    //Truy van goi can dang ky
    private fun queryTargetProduct(){}

    //kiem tra xem nguoi dung hien tai co dang dung goi nao k
    private fun queryCurrentPurchases(){}

    private fun handlerClaimSubscriptions() {
        if (targetProductDetails == null) {
            Toast.makeText(this, "Product not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val offerToken = getOfferTokenWithFreeTrial() ?: run {
            Toast.makeText(this, "No free trial offer", Toast.LENGTH_SHORT).show()
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(targetProductDetails!!)
            .setOfferToken(offerToken)
            .build()

        // Xây dựng SubscriptionUpdateParams
        val builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))

        currentPurchase?.let { purchase ->
            //Neu nguoi dung dang co goi tuy chon 5 TH de dang ky
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(purchase.purchaseToken)
                .setSubscriptionReplacementMode(getProrationMode())
                .build()
            builder.setSubscriptionUpdateParams(updateParams)
        }

        val billingFlowParams = builder.build()
        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    private fun getOfferTokenWithFreeTrial(): String? {
        val offers = targetProductDetails?.subscriptionOfferDetails ?: return null
        return offers.find { offer ->
            offer.pricingPhases.pricingPhaseList.any { phase ->
                phase.priceAmountMicros == 0L && phase.recurrenceMode == 2
            }
        }?.offerToken
    }

    private fun getProrationMode(): Int {
        return when ((0..4).random()) {
            0 -> {
                Toast.makeText(this, "WITH_TIME_PRORATION", Toast.LENGTH_LONG).show()
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION
            }
            1 -> {
                Toast.makeText(this, "CHARGE_PRORATED_PRICE", Toast.LENGTH_LONG).show()
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
            }
            2 -> {
                Toast.makeText(this, "WITHOUT_PRORATION", Toast.LENGTH_LONG).show()
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITHOUT_PRORATION
            }
            3 -> {
                Toast.makeText(this, "CHARGE_FULL_PRICE", Toast.LENGTH_LONG).show()
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
            }
            4 -> {
                Toast.makeText(this, "DEFERRED", Toast.LENGTH_LONG).show()
                BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED
            }
            else -> BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED
        }
    }

    private fun setupClickListeners() = with(binding) {
        btnTryTree.setOnClickListener {
            // handleClaimOffer()
        }
    }

    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        btnTryTree.isEnabled = false
        btnTryTree.text = ""
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnTryTree.isEnabled = true
        btnTryTree.text = getString(R.string.paywall_onboarding_btn_free)
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {}
}
