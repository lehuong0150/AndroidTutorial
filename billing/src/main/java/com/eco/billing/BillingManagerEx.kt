package com.eco.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.eco.billing.model.IN_APP
import com.eco.billing.model.SUBS
import com.eco.billing.state.BillingPurchasesState
import com.eco.inappbilling.BillingQueryState
import com.eco.billing.model.ProductInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.filterNot
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.text.set

fun BillingManager.queryAlls(
    identity: String,
    cached: Boolean = true,
    productInfos: List<ProductInfo>,
    onDataState: ((BillingQueryState) -> Unit)
) {
    connectToGooglePlay(identity) {
        billingQueryListeners[identity] = onDataState
        queryPurchasesAwaitAll(identity)
        queryProductDetailsAwaitAll(identity, cached, productInfos)
    }
}

fun BillingManager.queryProductDetails(
    identity: String,
    cached: Boolean = true,
    productInfos: List<ProductInfo>,
    onDataState: ((BillingQueryState) -> Unit)? = null
) {
    connectToGooglePlay(identity) {
        onDataState?.let { billingQueryListeners[identity] = it }
        queryProductDetailsAwaitAll(identity, cached, productInfos)
    }
}

fun BillingManager.queryPurchases(
    identity: String,
    onDataState: ((BillingQueryState) -> Unit)
) {
    connectToGooglePlay(identity) {
        billingQueryListeners[identity] = onDataState
        queryPurchasesAwaitAll(identity)
    }
}

fun BillingManager.buy(
    activity: Activity,
    productDetails: ProductDetails?,
    offerToken: String?,
    onBillingPurchasesListener: ((BillingPurchasesState) -> Unit)
) {
    billingPurchasesListener = onBillingPurchasesListener
    if (productDetails == null || offerToken == null) return
    val productDetailsParamsBuilder = ProductDetailsParams.newBuilder()
        .setProductDetails(productDetails)

    if (productDetails.productType == SUBS) {
        productDetailsParamsBuilder.setOfferToken(offerToken)
    }

    val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()

    billingClient.launchBillingFlow(activity, billingFlowParams)
}

fun BillingManager.buy(
    activity: Activity,
    productId: String,
    onBillingPurchasesListener: ((BillingPurchasesState) -> Unit)
) {
    val productDetails = detailsMutableMap[productId] ?: return
    billingPurchasesListener = onBillingPurchasesListener
    val productDetailsParamsBuilder = ProductDetailsParams.newBuilder()
        .setProductDetails(productDetails)

    // to get an offer token, call ProductDetails.subscriptionOfferDetails()
    // for a list of offers that are available to the user
    if (productDetails.productType == SUBS) {
        productDetails.subscriptionOfferDetails?.let { subscriptionOfferDetails ->
            val offerToken = subscriptionOfferDetails[0].offerToken
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }
    }

    val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()

    billingClient.launchBillingFlow(activity, billingFlowParams)
}

fun BillingManager.handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        val productId = purchase.products.first()
        val productInfo = productInfoMutableMap[productId]
        if (productInfo != null && productInfo.consumable) {
            consumePurchase(purchase)
        } else if (purchase.isAcknowledged) {
            onBillingPurchasesStateAcknowledged(productId, purchase)
        } else acknowledgePurchase(purchase)
    } else {
        onBillingPurchasesStateError(
            Exception("Purchase state not equals PURCHASED. Purchase state: ${purchase.purchaseState}")
        )
    }
}

fun BillingManager.consumePurchase(purchase: Purchase) {
    onBillingPurchasesStateLoading(true)
    val consumeParams =
        ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

    billingClient.consumeAsync(
        consumeParams
    ) { billingResult, _ ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val productId = purchase.products.first()
            onBillingPurchasesStateLoading(false)
            onBillingPurchasesStateAcknowledged(productId, purchase)
        } else {
            onBillingPurchasesStateError(
                Exception("Consume purchase error code ${billingResult.responseCode}")
            )
        }
    }
}

fun BillingManager.onBillingPurchasesStateAcknowledged(
    productId: String,
    purchase: Purchase
) {
    val state = BillingPurchasesState.PurchaseAcknowledged(productId, purchase)
    billingPurchasesListener?.invoke(state)
}

fun BillingManager.onBillingPurchasesStateLoading(isLoading: Boolean) {
    val state = BillingPurchasesState.AcknowledgePurchaseLoading(isLoading)
    billingPurchasesListener?.invoke(state)
}

fun BillingManager.onBillingPurchasesStateUserCancel() {
    val state = BillingPurchasesState.UserCancelPurchase
    billingPurchasesListener?.invoke(state)
}

fun BillingManager.onBillingPurchasesStateError(e: Exception) {
    val state = BillingPurchasesState.Error(e)
    billingPurchasesListener?.invoke(state)
}

private fun BillingManager.acknowledgePurchase(purchase: Purchase) {
    onBillingPurchasesStateLoading(true)

    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)

    coroutineScope.launch {
        try {
            val billingResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                withContext(Dispatchers.Main) {
                    val productId = purchase.products.first()
                    onBillingPurchasesStateLoading(false)
                    onBillingPurchasesStateAcknowledged(productId, purchase)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onBillingPurchasesStateError(
                        Exception("Acknowledge purchase error code ${billingResult.responseCode}")
                    )
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onBillingPurchasesStateError(e)
            }
        }
    }
}

private fun BillingManager.connectToGooglePlay(
    identity: String,
    connected: (() -> Unit)
) {
    if (billingClient.isReady) {
        connected.invoke()
        return
    }

    if (identity.isNotEmpty()) connectionListeners[identity] = connected

    if (!isConnecting) {
        isConnecting = true
        billingClient.startConnection(getBillingClientStateListener())
    }
}

private fun BillingManager.queryPurchasesAwaitAll(identity: String) =
    coroutineScope.launch {
        try {
            val inAppParams = QueryPurchasesParams.newBuilder().setProductType(IN_APP).build()
            val subsParams = QueryPurchasesParams.newBuilder().setProductType(SUBS).build()

            val deferredInApp = async { queryPurchasesInternal(inAppParams) }
            val deferredSubs = async { queryPurchasesInternal(subsParams) }
            val result = awaitAll(deferredInApp, deferredSubs).flatten()
            withContext(Dispatchers.Main) {
                val onDataState = billingQueryListeners[identity]
                onDataState?.invoke(BillingQueryState.PurchaseComplete(result))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val onDataState = billingQueryListeners[identity]
                onDataState?.invoke(BillingQueryState.PurchaseComplete(emptyList()))
                onDataState?.invoke(BillingQueryState.Error(e))
            }
        }
    }

private suspend fun BillingManager.queryPurchasesInternal(purchasesParams: QueryPurchasesParams): List<Purchase> {
    val purchasesResult = billingClient.queryPurchasesAsync(purchasesParams)
    return purchasesResult.purchasesList.filter {
        it.products.isNotEmpty() && it.purchaseState == Purchase.PurchaseState.PURCHASED
    }
}

private fun BillingManager.queryProductDetailsAwaitAll(
    identity: String,
    cached: Boolean,
    productInfos: List<ProductInfo>
) = coroutineScope.launch {
    try {
        productInfos.forEach { productInfoMutableMap[it.productId] = it }

        // Chỉ giữ các product chưa có trong cache
        val missingProductInfos = productInfos.filterNot {
            cached && detailsMutableMap.containsKey(it.productId)
        }

        // Chia nhỏ theo loại
        val inAppProducts = missingProductInfos.filter { it.productType == IN_APP }
        val subsProducts = missingProductInfos.filter { it.productType == SUBS }

        // Gọi query song song
        val deferredInApp = async { queryProductDetailsInternal(inAppProducts) }
        val deferredSubs = async { queryProductDetailsInternal(subsProducts) }

        val queried = awaitAll(deferredInApp, deferredSubs).flatten()

        // Cập nhật cache
        queried.forEach { detailsMutableMap[it.productId] = it }

        // Kết quả cuối cùng: lấy từ cache
        val result = productInfos.mapNotNull { detailsMutableMap[it.productId] }

        withContext(Dispatchers.Main) {
            val onDataState = billingQueryListeners[identity]
            onDataState?.invoke(BillingQueryState.ProductDetailsComplete(result))
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            val onDataState = billingQueryListeners[identity]
            onDataState?.invoke(BillingQueryState.ProductDetailsComplete(emptyList()))
            onDataState?.invoke(BillingQueryState.Error(e))
        }
    }
}

private suspend fun BillingManager.queryProductDetailsInternal(productInfos: List<ProductInfo>): List<ProductDetails> {
    if (productInfos.isEmpty()) return emptyList()

    val products = productInfos.map {
        Product.newBuilder()
            .setProductId(it.productId)
            .setProductType(it.productType)
            .build()
    }

    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(products)
        .build()

    val result = billingClient.queryProductDetails(params)
    return result.productDetailsList ?: emptyList()
}

private fun BillingManager.getBillingClientStateListener() =
    object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            isConnecting = false
            connectionListeners.clear()
        }

        override fun onBillingSetupFinished(result: BillingResult) {
            isConnecting = false
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val listeners = connectionListeners.values.toList()
                connectionListeners.clear()
                listeners.forEach { it() }
            } else {
                connectionListeners.clear()
            }
        }
    }