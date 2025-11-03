package com.eco.inappbilling

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

sealed class BillingQueryState {
    data class ProductDetailsComplete(val products: List<ProductDetails>): BillingQueryState()
    data class PurchaseComplete(val purchases: List<Purchase>):BillingQueryState()
    data class Error(val exception: Exception):BillingQueryState()
}