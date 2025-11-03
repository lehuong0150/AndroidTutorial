package com.eco.billing.model

import androidx.annotation.Keep

@Keep
data class ProductInfo(
    var productType: String = SUBS,
    var productId: String = "",
    var consumable: Boolean = false
)