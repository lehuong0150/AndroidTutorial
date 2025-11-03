package com.eco.billing.state

import com.android.billingclient.api.Purchase

sealed class BillingPurchasesState {

    data class PurchaseAcknowledged(val productId: String, val purchase: Purchase) :
        BillingPurchasesState()

    data class AcknowledgePurchaseLoading(val isLoading: Boolean) : BillingPurchasesState()

    data object UserCancelPurchase : BillingPurchasesState()

    data class Error(val exception: Exception) : BillingPurchasesState()
}