package com.eco.musicplayer.audioplayer.music.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.eco.musicplayer.audioplayer.music.models.OfferInfo
import com.eco.musicplayer.audioplayer.music.utils.OfferType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val listener: BillingListener
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private val productDetailsList = mutableListOf<ProductDetails>()
    private var bestWeeklyOffer: OfferInfo? = null
    private val weeklyOffers = mutableListOf<OfferInfo>()

    companion object {
        private const val TAG = "BillingManager"

        // Product IDs
        const val SUB_PRODUCT_ID = "free_123"
        const val LIFETIME_PRODUCT_ID = "test3"

        // Offer IDs (chỉ cho subscription)
        const val OFFER_3_DAYS = "3days"
        const val OFFER_7_DAYS = "7days"
        const val OFFER_INTRO_PRICE = "intro-price"
        const val OFFER_MINUS = "test1"
    }

    fun initialize() {

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToBilling()
    }

    private fun connectToBilling() {
        Log.d(TAG, "--- Connecting to Billing ---")
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful")
                    listener.onBillingSetupFinished(true)
                    queryProducts()
                    checkPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.responseCode}")
                    listener.onBillingSetupFinished(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected - retrying")
                connectToBilling()
            }
        })
    }

    private fun queryProducts() {
        Log.d(TAG, "--- queryProducts ---")
        Log.d(TAG, "Subscription ID: $SUB_PRODUCT_ID")
        Log.d(TAG, "Lifetime ID: $LIFETIME_PRODUCT_ID")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                productDetailsList.clear()

                // Query Subscription (Weekly)
                val subscriptionResult = querySubscriptionProducts()
                subscriptionResult?.let { productDetailsList.addAll(it) }

                // Query Lifetime (One-time purchase)
                val lifetimeResult = queryLifetimeProducts()
                lifetimeResult?.let { productDetailsList.addAll(it) }

                if (productDetailsList.isEmpty()) {
                    Log.e(TAG, "No products found!")
                    withContext(Dispatchers.Main) {
                        listener.onProductDetailsLoaded("", "", null)
                    }
                } else {
                    Log.d(TAG, "Total products loaded: ${productDetailsList.size}")
                    parseAndNotifyPrices(productDetailsList)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception in queryProducts: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    listener.onProductDetailsLoaded("", "", null)
                }
            }
        }
    }

    private suspend fun querySubscriptionProducts(): List<ProductDetails>? {
        return try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(SUB_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetails(params)
            }

            if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Subscription products: ${result.productDetailsList?.size ?: 0}")
                result.productDetailsList
            } else {
                Log.e(TAG, "Subscription query failed: ${result?.billingResult?.debugMessage}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying subscriptions: ${e.message}")
            null
        }
    }

    private suspend fun queryLifetimeProducts(): List<ProductDetails>? {
        return try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(LIFETIME_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetails(params)
            }

            if (result?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Lifetime products: ${result.productDetailsList?.size ?: 0}")
                result.productDetailsList
            } else {
                Log.e(TAG, "Lifetime query failed: ${result?.billingResult?.debugMessage}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying lifetime: ${e.message}")
            null
        }
    }

    private suspend fun parseAndNotifyPrices(products: List<ProductDetails>) {
        Log.d(TAG, "=========================")
        Log.d(TAG, "Parsing ${products.size} products")
        Log.d(TAG, "=========================")

        var weeklyPrice = ""
        var lifetimePrice = ""
        weeklyOffers.clear()

        products.forEach { product ->
            Log.d(TAG, "\n>>> PRODUCT: ${product.productId}")
            Log.d(TAG, "Type: ${product.productType}")
            Log.d(TAG, "Title: ${product.name}")
            Log.d(TAG, "Description: ${product.description}")

            when (product.productType) {
                BillingClient.ProductType.INAPP -> {
                    product.oneTimePurchaseOfferDetails?.let { offer ->
                        lifetimePrice = offer.formattedPrice
                        Log.d(
                            TAG,
                            "ONE-TIME OFFER => Price: ${offer.formattedPrice}, Currency: ${offer.priceCurrencyCode}"
                        )
                    }
                }

                BillingClient.ProductType.SUBS -> {
                    val offerList = product.subscriptionOfferDetails
                    if (offerList.isNullOrEmpty()) {
                        Log.w(TAG, "No subscription offers found for ${product.productId}")
                        return@forEach
                    }

                    offerList.forEachIndexed { index, offer ->
                        Log.d(TAG, "\n--- OFFER #${index + 1} ---")
                        Log.d(TAG, "Offer ID: ${offer.offerId ?: "null"}")
                        Log.d(TAG, "Offer Token: ${offer.offerToken}")
                        Log.d(TAG, "Base Plan ID: ${offer.basePlanId}")
                        Log.d(TAG, "Tags: ${offer.offerTags.joinToString()}")

                        val pricingPhases = offer.pricingPhases.pricingPhaseList
                        pricingPhases.forEachIndexed { phaseIndex, phase ->
                            val amount = phase.priceAmountMicros / 1_000_000.0
                            Log.d(TAG, "   Phase ${phaseIndex + 1}:")
                            Log.d(
                                TAG,
                                "     Price: ${phase.formattedPrice} ($amount ${phase.priceCurrencyCode})"
                            )
                            Log.d(TAG, "     Billing period: ${phase.billingPeriod}")
                            Log.d(TAG, "     Cycle count: ${phase.billingCycleCount}")
                            Log.d(TAG, "     Recurrence mode: ${phase.recurrenceMode}")
                        }

                        val firstPhase = pricingPhases.firstOrNull()
                        val hasFreeTrial = pricingPhases.any { it.priceAmountMicros == 0L }
                        val offerType = when {
                            hasFreeTrial -> OfferType.FREE_TRIAL
                            pricingPhases.size > 1 -> OfferType.INTRO_PRICE
                            else -> OfferType.NORMAL
                        }

                        val finalPhase = pricingPhases.lastOrNull()
                        val finalPrice = finalPhase?.formattedPrice ?: "N/A"

                        val freeTrialDays = pricingPhases
                            .filter { it.priceAmountMicros == 0L }
                            .sumOf { parseDaysFromPeriod(it.billingPeriod) }

                        val introDays = pricingPhases
                            .filter { it.priceAmountMicros > 0L && it != finalPhase }
                            .sumOf { parseDaysFromPeriod(it.billingPeriod) * it.billingCycleCount }

                        val offerInfo = OfferInfo(
                            offerId = offer.offerId ?: "",
                            offerType = offerType,
                            freeTrialDays = freeTrialDays,
                            introPriceDays = introDays,
                            introPrice = firstPhase?.formattedPrice ?: "",
                            formattedPrice = finalPrice,
                            offerToken = offer.offerToken
                        )
                        weeklyOffers.add(offerInfo)

                        Log.d(
                            TAG,
                            "Summary -> Type: $offerType, Free trial: ${freeTrialDays}d, Intro: ${introDays}d, Final: $finalPrice"
                        )
                    }

                    val defaultOffer =
                        weeklyOffers.maxByOrNull { it.freeTrialDays } ?: weeklyOffers.firstOrNull()
                    weeklyPrice = defaultOffer?.formattedPrice ?: ""
                    bestWeeklyOffer = defaultOffer
                }
            }
        }

        Log.d(TAG, "\n=========================")
        Log.d(TAG, "Final Prices Summary:")
        Log.d(TAG, "Weekly Price: $weeklyPrice")
        Log.d(TAG, "Lifetime Price: $lifetimePrice")
        Log.d(TAG, "Best Weekly Offer: ${bestWeeklyOffer?.offerId}")
        Log.d(TAG, "=========================\n")

        withContext(Dispatchers.Main) {
            val defaultOffer = weeklyOffers.maxByOrNull { it.freeTrialDays }
            listener.onProductDetailsLoaded(weeklyPrice, lifetimePrice, defaultOffer)
        }
    }

    fun getOfferByOfferId(offerId: String): OfferInfo? {
        return weeklyOffers.find { it.offerId == offerId }
    }

    fun getOfferByTrialDays(days: Int): OfferInfo? {
        return weeklyOffers.find { it.freeTrialDays == days }
    }

    private fun parseDaysFromPeriod(period: String): Int {
        return when {
            period.contains("P") && period.contains("D") -> {
                period.replace("P", "").replace("D", "").toIntOrNull() ?: 0
            }

            period.contains("P1W") -> 7
            period.contains("P1M") -> 30
            else -> 0
        }
    }

    fun launchPurchaseFlow(activity: Activity, isLifetime: Boolean, offerId: String = "") {
        Log.d(TAG, "--- Launch Purchase ---")
        Log.d(TAG, "Is Lifetime: $isLifetime")
        Log.d(TAG, "Offer ID: $offerId")

        val productDetails = if (isLifetime) {
            productDetailsList.find { it.productId == LIFETIME_PRODUCT_ID }
        } else {
            productDetailsList.find { it.productId == SUB_PRODUCT_ID }
        }

        if (productDetails == null) {
            Log.e(TAG, "Product not found!")
            listener.onPurchaseFailed("Sản phẩm chưa sẵn sàng")
            return
        }

        if (isLifetime) {
            // One-time purchase
            launchOneTimePurchase(activity, productDetails)
        } else {
            // Subscription
            launchSubscriptionPurchase(activity, productDetails, offerId)
        }
    }

    private fun launchOneTimePurchase(activity: Activity, productDetails: ProductDetails) {
        val params = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(params)
            .build()

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)

        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            listener.onPurchaseFailed("Không thể khởi tạo thanh toán")
        }
    }

    private fun launchSubscriptionPurchase(
        activity: Activity,
        productDetails: ProductDetails,
        offerId: String
    ) {
        val selectedOffer = if (offerId.isNotEmpty()) {
            productDetails.subscriptionOfferDetails?.find { offer ->
                offer.offerId == offerId || offer.offerTags.contains(offerId)
            }
        } else {
            productDetails.subscriptionOfferDetails?.firstOrNull()
        }

        if (selectedOffer == null) {
            listener.onPurchaseFailed("Không tìm thấy gói phù hợp")
            return
        }

        Log.d(TAG, "Selected offer: ${selectedOffer.offerId}")

        val params = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(selectedOffer.offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(params)
            .build()

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)

        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            listener.onPurchaseFailed("Không thể khởi tạo thanh toán")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                listener.onPurchaseFailed("Đã hủy thanh toán")
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                listener.onPurchaseSuccess()
            }

            else -> {
                listener.onPurchaseFailed("Thanh toán thất bại: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                listener.onPurchaseSuccess()
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val result = billingClient?.acknowledgePurchase(params)

                withContext(Dispatchers.Main) {
                    if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                        listener.onPurchaseSuccess()
                    } else {
                        listener.onPurchaseFailed("Xác nhận thất bại")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error acknowledging: ${e.message}")
                withContext(Dispatchers.Main) {
                    listener.onPurchaseFailed("Lỗi xác nhận: ${e.message}")
                }
            }
        }
    }

    private fun checkPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check subscriptions
                val subParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
                val subResult = billingClient?.queryPurchasesAsync(subParams)
                subResult?.purchasesList?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Active subscription: ${purchase.products}")
                    }
                }

                // Check lifetime
                val inAppParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                val inAppResult = billingClient?.queryPurchasesAsync(inAppParams)
                inAppResult?.purchasesList?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Lifetime purchase: ${purchase.products}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking purchases: ${e.message}")
            }
        }
    }

    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }
}