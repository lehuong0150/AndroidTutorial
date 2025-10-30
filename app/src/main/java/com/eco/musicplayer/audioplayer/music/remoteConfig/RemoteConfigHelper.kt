package com.eco.musicplayer.audioplayer.music.remoteConfig

import android.content.ContentValues.TAG
import android.util.Log
import com.eco.musicplayer.audioplayer.music.models.paywall.InAppProduct
import com.eco.musicplayer.audioplayer.music.models.paywall.PaywallConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object RemoteConfigHelper {
    private val gson by lazy { Gson() }
    fun getString(key: String, default: String = ""): String {
        return Firebase.remoteConfig.getString(key).ifEmpty { default }
    }

    fun getInt(key: String, default: Int = 0): Int {
        val value = Firebase.remoteConfig.getLong(key).toInt()
        return value.takeIf { it != 0 } ?: default
    }

    fun getLong(key: String): Long {
        return Firebase.remoteConfig.getLong(key)
    }

    fun getBoolean(key: String): Boolean {
        return Firebase.remoteConfig.getBoolean(key)
    }

    fun getPaywallConfig(key: String = "pricing_config"): PaywallConfig {
        val jsonString = getString(key)
        Log.d(TAG, "getPaywallConfig JSON: $jsonString")

        return try {
            if (jsonString.isNotEmpty()) {
                gson.fromJson(jsonString, PaywallConfig::class.java).also { config ->
                }
            } else {
                getDefaultPaywallConfig()
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing paywall config: ${e.message}", e)
            getDefaultPaywallConfig()
        }
    }

    private fun getDefaultPaywallConfig(): PaywallConfig {
        return PaywallConfig(
            uiType = "6",
            products = listOf(
                InAppProduct(
                    productId = "free_123",
                    offerId = "3days",
                    productType = "subs"
                ),
                InAppProduct(
                    productId = "test3",
                    offerId = "",
                    productType = "inapp"
                )
            ),
            selectionPosition = 1
        )
    }
}