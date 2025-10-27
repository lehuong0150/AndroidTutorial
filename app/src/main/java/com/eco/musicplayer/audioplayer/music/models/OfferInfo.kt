package com.eco.musicplayer.audioplayer.music.models

data class OfferInfo(
    val offerId: String,
    val freeTrialDays: Int,        // Số ngày dùng thử miễn phí
    val formattedPrice: String,
    val offerToken: String
)
