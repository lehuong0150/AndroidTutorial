package com.eco.musicplayer.audioplayer.music.billing

import com.eco.musicplayer.audioplayer.music.models.OfferInfo

interface BillingListener {
    fun onBillingSetupFinished(isSuccess: Boolean)

    fun onProductDetailsLoaded(
        weeklyPrice: String,
        lifetimePrice: String,
        weeklyOffer: OfferInfo?
    )

    fun onPurchaseSuccess()
    fun onPurchaseFailed(errorMessage: String)
}