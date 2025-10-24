package com.eco.musicplayer.audioplayer.music.layout

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.*
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityUnlockBinding
import kotlinx.coroutines.*

class UnlockActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var binding: ActivityUnlockBinding
    private lateinit var billingClient: BillingClient
    private var currentPurchase: Purchase? = null
    private var targetProductDetails: ProductDetails? = null
    private var hasActiveSubscription = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        showLoading()
        binding.root.postDelayed({
            showSuccess()
        }, 1500)

        setupBillingClient()
        setupClickListeners()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Toast.makeText(this@UnlockActivity, "Billing service disconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    CoroutineScope(Dispatchers.Main).launch {
                        queryTargetProduct()
                        queryCurrentPurchases()
                    }
                }
            }
        })
    }

    private suspend fun queryTargetProduct() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("free_123") //
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params)
        }

        withContext(Dispatchers.Main) {
            if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = productDetailsResult.productDetailsList
                if (!list.isNullOrEmpty()) {
                    targetProductDetails = list.first()
                    showSuccess()
                    Toast.makeText(this@UnlockActivity, "Product details loaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@UnlockActivity, "No products found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    this@UnlockActivity,
                    "Error: ${productDetailsResult.billingResult.debugMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun queryCurrentPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasActiveSubscription = purchasesList.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && it.isAcknowledged
                }
                if (hasActiveSubscription) {
                    Toast.makeText(this, "Đã có gói đang hoạt động", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Chưa đăng ký gói", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleClaimSubscription() {
        if (targetProductDetails == null) {
            Toast.makeText(this, "Product not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val offerToken = getFreeTrialOfferToken() ?: run {
            Toast.makeText(this, "No free trial offer", Toast.LENGTH_SHORT).show()
            return
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(targetProductDetails!!)
            .setOfferToken(offerToken)
            .build()

        val builder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))

        currentPurchase?.let { purchase ->
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(purchase.purchaseToken)
                .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION)
                .build()
            builder.setSubscriptionUpdateParams(updateParams)
        }

        billingClient.launchBillingFlow(this, builder.build())
    }

    private fun getFreeTrialOfferToken(): String? {
        val offers = targetProductDetails?.subscriptionOfferDetails ?: return null
        return offers.find { offer ->
            offer.pricingPhases.pricingPhaseList.any { phase ->
                phase.priceAmountMicros == 0L && phase.recurrenceMode == 2
            }
        }?.offerToken
    }

    private fun setupClickListeners() = with(binding) {
        btnTryTree.setOnClickListener { handleClaimSubscription() }
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

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            CoroutineScope(Dispatchers.Main).launch {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        currentPurchase = purchase
                        if (!purchase.isAcknowledged) {
                            val params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(params) { ack ->
                                if (ack.responseCode == BillingClient.BillingResponseCode.OK) {
                                    Toast.makeText(
                                        this@UnlockActivity,
                                        "Giao dịch được xác nhận",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        Toast.makeText(
                            this@UnlockActivity,
                            "Mua hàng thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        showSuccess()
                    }
                }
            }
        } else {
            val message = when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> "Mua hàng bị hủy"
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Dịch vụ không khả dụng"
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Thanh toán không khả dụng"
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Sản phẩm không khả dụng"
                else -> "Lỗi: ${billingResult.debugMessage}"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
