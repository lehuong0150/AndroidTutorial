package com.eco.musicplayer.audioplayer.music.models

import com.eco.musicplayer.audioplayer.music.utils.OfferType

data class OfferInfo(
    val offerId: String,
    val offerType: OfferType,
    val freeTrialDays: Int = 0,
    val introPriceDays: Int = 0,
    val introPriceCycle: Int = 0,
    val introPrice: String = "",
    val formattedPrice: String,
    val offerToken: String
)