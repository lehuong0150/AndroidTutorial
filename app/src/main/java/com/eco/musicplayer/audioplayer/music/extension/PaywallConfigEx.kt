package com.eco.musicplayer.audioplayer.music.extension

import com.eco.musicplayer.audioplayer.music.models.paywall.InAppProduct
import com.eco.musicplayer.audioplayer.music.models.paywall.PaywallConfig

fun PaywallConfig.getSubscriptionIds(): List<String> {
    return products?.filter {
        it.productType == "subs"
    }?.mapNotNull { it.productId } ?: emptyList()
}

fun PaywallConfig.getLifetimeIds(): List<String> {
    return products?.filter {
        it.productType == "inapp"
    }?.mapNotNull { it.productId } ?: emptyList()
}

fun PaywallConfig.getLifetimeId(): String? = getLifetimeIds().firstOrNull()

fun PaywallConfig.getOfferIdForProduct(productId: String): String =
    products?.find { it.productId == productId }?.offerId?.trim() ?: ""

fun PaywallConfig.getSelectedProduct(): InAppProduct? =
    products?.getOrNull(selectionPosition ?: 0)

fun PaywallConfig.hasLifetime(): Boolean = getLifetimeId() != null

fun PaywallConfig.hasSubscription(): Boolean = getSubscriptionIds().isNotEmpty()

//fun PaywallConfig.logProducts(tag: String = "PaywallConfig") {
//    android.util.Log.d(tag, "===== Config Products =====")
//    products?.forEachIndexed { index, product ->
//        android.util.Log.d(
//            tag,
//            "[$index] ID: ${product.productId}, Type: ${product.productType}, Offer: ${product.offerId}"
//        )
//    }
//    android.util.Log.d(tag, "---")
//    android.util.Log.d(tag, "Subscription IDs: ${getSubscriptionIds()}")
//    getSubscriptionIds().forEachIndexed { index, subId ->
//        android.util.Log.d(tag, "  Sub[$index]: $subId → Offer: ${getOfferIdForProduct(subId)}")
//    }
//    android.util.Log.d(tag, "Lifetime ID: ${getLifetimeId()}")
//    android.util.Log.d(tag, "Selection Position: $selectionPosition")
//    android.util.Log.d(tag, "===========================")
//}