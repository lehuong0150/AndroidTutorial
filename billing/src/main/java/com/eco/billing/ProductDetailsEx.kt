package com.eco.billing

import com.android.billingclient.api.ProductDetails
import com.eco.billing.model.ProductDetailsOffer
import org.threeten.bp.Period

fun ProductDetails.asProductDetailsOffer(offerId: String = ""): ProductDetailsOffer {
    oneTimePurchaseOfferDetails?.let {
        return ProductDetailsOffer(
            productDetails = this,
            typePeriod = ProductDetailsOffer.TypePeriod.LIFETIMES,
            formattedPrice = it.formattedPrice,
            priceAmountMicros = it.priceAmountMicros,
            currencySymbol = "",
            priceCurrencyCode = it.priceCurrencyCode
        )
    }

    this.subscriptionOfferDetails?.let { list ->
        val subscriptionOfferDetails = list.find { offerId.isNotEmpty() && it.offerId == offerId }
            ?: list.find {
                val phases = it.pricingPhases.pricingPhaseList
                phases.size == 1 && phases.first().priceAmountMicros > 0
            } ?: list.last()

        val pricingPhaseList = subscriptionOfferDetails.pricingPhases.pricingPhaseList

        if (subscriptionOfferDetails.offerId == offerId && pricingPhaseList.size > 1) {
            return if (pricingPhaseList.size == 3) {
                productDetailsCustomTwoPhase(pricingPhaseList, subscriptionOfferDetails)
            } else productDetailsCustomOnePhase(pricingPhaseList, subscriptionOfferDetails)
        } else {
            val basePhase = pricingPhaseList.last()
            return ProductDetailsOffer(
                productDetails = this,
                typePeriod = basePhase.getType(),
                formattedPrice = basePhase.formattedPrice,
                priceAmountMicros = basePhase.priceAmountMicros,
                date = basePhase.getDate(),
                offerToken = subscriptionOfferDetails.offerToken,
                currencySymbol = "",
                priceCurrencyCode = basePhase.priceCurrencyCode
            )
        }
    }

    return ProductDetailsOffer(
        productDetails = this,
        typePeriod = ProductDetailsOffer.TypePeriod.NONE,
        formattedPrice = "",
        priceAmountMicros = 0L,
        priceCurrencyCode = ""
    )
}

private fun ProductDetails.productDetailsCustomOnePhase(
    pricingPhaseList: List<ProductDetails.PricingPhase>,
    subscriptionOfferDetails: ProductDetails.SubscriptionOfferDetails
): ProductDetailsOffer {
    val first = pricingPhaseList.first()
    val last = pricingPhaseList.last()

    val dayFreeTrial = first.getDate()
    if (first.priceAmountMicros == 0L && dayFreeTrial > 0) {
        // Free trial
        return ProductDetailsOffer(
            productDetails = this,
            typePeriod = last.getType(),
            formattedPrice = last.formattedPrice,
            priceAmountMicros = last.priceAmountMicros,
            date = last.getDate(),
            offerToken = subscriptionOfferDetails.offerToken,
            dayFreeTrial = dayFreeTrial,
            typeOffer = ProductDetailsOffer.TypeOffer.FREE_TRIAL,
            currencySymbol = "",
            priceCurrencyCode = last.priceCurrencyCode
        )
    } else {
        // Discount offer
        return ProductDetailsOffer(
            productDetails = this,
            typePeriod = last.getType(),
            formattedPrice = last.formattedPrice,
            formattedPriceOffer = first.formattedPrice,
            priceAmountMicros = last.priceAmountMicros,
            priceAmountMicrosOffer = first.priceAmountMicros,
            typeOffer = ProductDetailsOffer.TypeOffer.OFFER,
            date = last.getDate(),
            offerToken = subscriptionOfferDetails.offerToken,
            currencySymbol = "",
            priceCurrencyCode = last.priceCurrencyCode
        )
    }
}

private fun ProductDetails.productDetailsCustomTwoPhase(
    pricingPhaseList: List<ProductDetails.PricingPhase>,
    subscriptionOfferDetails: ProductDetails.SubscriptionOfferDetails
): ProductDetailsOffer {
    val first = pricingPhaseList.first()
    val between = pricingPhaseList[1]
    val last = pricingPhaseList.last()

    return ProductDetailsOffer(
        productDetails = this,
        typePeriod = last.getType(),
        formattedPrice = last.formattedPrice,
        formattedPriceOffer = between.formattedPrice,
        priceAmountMicros = last.priceAmountMicros,
        priceAmountMicrosOffer = between.priceAmountMicros,
        date = last.getDate(),
        offerToken = subscriptionOfferDetails.offerToken,
        dayFreeTrial = first.getDate(),
        typeOffer = ProductDetailsOffer.TypeOffer.FREE_TRIAL_AND_OFFER,
        currencySymbol = "",
        priceCurrencyCode = last.priceCurrencyCode
    )
}

private fun ProductDetails.PricingPhase.getType(): ProductDetailsOffer.TypePeriod {
    return when {
        billingPeriod.contains("D") -> ProductDetailsOffer.TypePeriod.WEEK
        billingPeriod.contains("W") -> ProductDetailsOffer.TypePeriod.WEEK
        billingPeriod.contains("M") -> ProductDetailsOffer.TypePeriod.MONTH
        billingPeriod.contains("Y") -> ProductDetailsOffer.TypePeriod.YEAR
        else -> ProductDetailsOffer.TypePeriod.NONE
    }
}

private fun ProductDetails.PricingPhase.getDate(): Int {
    val period = Period.parse(billingPeriod)
    val totalDays = period.years * 365 + period.months * 30 + period.days
    return totalDays
}

