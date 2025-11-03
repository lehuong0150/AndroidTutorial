package com.eco.billing

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.eco.billing.state.BillingPurchasesState
import com.eco.inappbilling.BillingQueryState
import com.eco.billing.model.ProductInfo
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager(private val context: Context) {

    init {
        AndroidThreeTen.init(context)
    }

    var isConnecting = false

    var billingPurchasesListener: ((BillingPurchasesState) -> Unit)? = null

    val detailsMutableMap = mutableMapOf<String, ProductDetails>()

    val productInfoMutableMap = mutableMapOf<String, ProductInfo>()

    val connectionListeners = mutableMapOf<String, () -> Unit>()
    val billingQueryListeners = mutableMapOf<String, (BillingQueryState) -> Unit>()

    val coroutineScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        coroutineScope.launch(Dispatchers.Main) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) handlePurchase(purchase)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                onBillingPurchasesStateUserCancel()
            } else {
                onBillingPurchasesStateError(Exception("Error: ${billingResult.responseCode}"))
            }
        }
    }

    val billingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans().build()
            )
            .build()
    }

    fun detachListeners(identity: String) {
        billingPurchasesListener = null
        billingQueryListeners.remove(identity)
        connectionListeners.remove(identity)
    }
}