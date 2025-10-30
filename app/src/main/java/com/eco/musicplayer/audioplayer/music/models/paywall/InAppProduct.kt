package com.eco.musicplayer.audioplayer.music.models.paywall

import com.google.gson.annotations.SerializedName

data class InAppProduct(
    @SerializedName("productId")
    val productId: String? = "",

    @SerializedName("offerId")
    val offerId: String? = "",

    @SerializedName("productType")
    val productType: String? = "subs"
)