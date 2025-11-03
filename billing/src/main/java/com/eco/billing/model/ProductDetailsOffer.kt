package com.eco.billing.model

import com.android.billingclient.api.ProductDetails

data class ProductDetailsOffer(
    val productDetails: ProductDetails,
    val typePeriod: TypePeriod,
    val date: Int = 0,
    val formattedPrice: String,
    val formattedPriceOffer: String = "",
    val priceAmountMicros:Long,
    val priceAmountMicrosOffer:Long = 0,
    val priceCurrencyCode:String,
    val typeOffer: TypeOffer = TypeOffer.NONE,
    val dayFreeTrial: Int = 0,
    val currencySymbol: String = "",
    val offerToken: String = ""
) {

    enum class TypePeriod {
        NONE,
        LIFETIMES,
        WEEK,
        MONTH,
        YEAR
    }

    enum class TypeOffer {
        NONE,
        FREE_TRIAL,
        FREE_TRIAL_AND_OFFER,
        OFFER
    }
}