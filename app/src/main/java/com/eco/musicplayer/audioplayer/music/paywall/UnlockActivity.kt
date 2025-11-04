package com.eco.musicplayer.audioplayer.music.paywall

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.ProductDetails
import com.eco.billing.BillingManager
import com.eco.billing.asProductDetailsOffer
import com.eco.billing.buy
import com.eco.billing.model.IN_APP
import com.eco.billing.model.ProductDetailsOffer
import com.eco.billing.model.ProductInfo
import com.eco.billing.model.SUBS
import com.eco.billing.queryAlls
import com.eco.billing.state.BillingPurchasesState
import com.eco.billing.state.BillingQueryState
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityUnlockBinding

class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding
    private val billingManager: BillingManager by lazy { BillingManager(this) }

    private val productInfos = listOf(
        ProductInfo(SUBS, "free_123"),
        ProductInfo(SUBS, "test2"),
        ProductInfo(IN_APP, "test1")
    )

    private val selectedProductId = "free_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        showLoading()

        // Gọi Billing ngay khi mở
        setUpBilling()

        setupClickListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setUpBilling() {
        billingManager.queryAlls(
            identity = "UnlockActivity",
            cached = true,
            productInfos = productInfos,
            onDataState = ::handleQueryState
        )
    }

    // XỬ LÝ KẾT QUẢ QUERY – ĐÃ SỬA `details` → `products`
    private fun handleQueryState(state: BillingQueryState) {
        when (state) {
            is BillingQueryState.ProductDetailsComplete -> {
                updateContentWithOffer(state.products) // ← SỬA: products, không phải details
                showSuccess()
            }

            is BillingQueryState.PurchaseComplete -> {
                finishWithSuccess()
            }

            is BillingQueryState.Error -> {
                showError("Lỗi kết nối: ${state.exception.message}")
            }

            else -> {}
        }
    }

    // CẬP NHẬT NÚT VỚI GIÁ + ƯU ĐÃI
    private fun updateContentWithOffer(productDetailsList: List<ProductDetails>) {
        val details = productDetailsList.find { it.productId == selectedProductId } ?: return
        val offer = details.asProductDetailsOffer()

        with(binding.txtContent) {
            text = when (offer.typeOffer) {
                ProductDetailsOffer.TypeOffer.FREE_TRIAL -> {
                    getString(R.string.unlock_content, offer.formattedPrice, offer.dayFreeTrial,offer.typePeriod)
                }
                ProductDetailsOffer.TypeOffer.OFFER -> {
                    getString(R.string.unlock_offer, offer.formattedPriceOffer, offer.formattedPrice)
                }
                else -> offer.formattedPrice
            }
        }
    }

    // MỞ THANH TOÁN
    private fun launchPurchase(productDetails: ProductDetails) {
        showLoading()

        val offerToken = productDetails.subscriptionOfferDetails
            ?.find { it.pricingPhases.pricingPhaseList.size > 1 }
            ?.offerToken
            ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: ""

        billingManager.buy(
            activity = this,
            productDetails = productDetails,
            offerToken = offerToken,
            onBillingPurchasesListener = ::handlePurchaseState
        )
    }

    // XỬ LÝ KẾT QUẢ MUA
    private fun handlePurchaseState(state: BillingPurchasesState) {
        when (state) {
            is BillingPurchasesState.AcknowledgePurchaseLoading -> showLoading()

            is BillingPurchasesState.PurchaseAcknowledged -> {
                showSuccess()
                Toast.makeText(this, "Mua thành công!", Toast.LENGTH_SHORT).show()
                finishWithSuccess()
            }

            is BillingPurchasesState.UserCancelPurchase -> {
                showSuccess()
                Toast.makeText(this, "Đã hủy", Toast.LENGTH_SHORT).show()
            }

            is BillingPurchasesState.Error -> {
                showError("Thanh toán lỗi: ${state.exception.message}")
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        btnTryTree.setOnClickListener {
            val details = billingManager.detailsMutableMap[selectedProductId]
            if (details != null) {
                launchPurchase(details)
            } else {
                showError("Sản phẩm chưa sẵn sàng")
            }
        }
    }

    // UI STATES
    private fun showLoading() = with(binding) {
        pgbLoadInfo.visibility = View.VISIBLE
        btnTryTree.isEnabled = false
        btnTryTree.text = null
    }

    private fun showSuccess() = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnTryTree.isEnabled = true
    }

    private fun showError(message: String) = with(binding) {
        pgbLoadInfo.visibility = View.INVISIBLE
        btnTryTree.isEnabled = true
        btnTryTree.text = getString(R.string.paywall_onboarding_btn_free)
        Toast.makeText(this@UnlockActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun finishWithSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        billingManager.detachListeners("UnlockActivity")
        super.onDestroy()
    }
}