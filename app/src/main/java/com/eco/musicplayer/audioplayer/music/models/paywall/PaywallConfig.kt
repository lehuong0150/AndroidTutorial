package com.eco.musicplayer.audioplayer.music.models.paywall

import com.google.gson.annotations.SerializedName

data class PaywallConfig(

    @SerializedName("uiType")
    val uiType: String? = null,

    @SerializedName("products")
    val products: List<InAppProduct>? = null,

    @SerializedName("selectionPosition")
    val selectionPosition: Int? = null
)