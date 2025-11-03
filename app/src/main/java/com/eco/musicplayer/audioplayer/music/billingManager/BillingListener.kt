package com.eco.musicplayer.audioplayer.music.billingManager

import com.eco.musicplayer.audioplayer.music.models.OfferInfo

interface BillingListener {
    fun onBillingSetupFinished(isSuccess: Boolean)

    fun onProductDetailsLoaded(
        weeklyPrice: String,
        lifetimePrice: String,
        weeklyOffer: OfferInfo?
    )

    fun checkTrialEligibility(hasUsedTrial: Boolean)
    fun onPurchaseSuccess()
    fun onPurchaseFailed(errorMessage: String)
}