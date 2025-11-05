package com.eco.musicplayer.audioplayer.music.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import com.eco.musicplayer.audioplayer.music.models.modelActivity.NetworkStateCallback

object NetworkUtils {
    private var previousNetworkType: String? = null

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return "No Connection"
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "No Connection"

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "3G"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.typeName ?: "No Connection"
        }
    }

    fun registerNetworkCallback(
        context: Context,
        callback: NetworkStateCallback
    ): ConnectivityManager.NetworkCallback {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkType = getNetworkType(context)
                if (networkType != previousNetworkType) {
                    Log.d(
                        "NetworkCallback",
                        "Network available: $networkType (changed from $previousNetworkType)"
                    )
                    previousNetworkType = networkType
                    callback.onNetworkAvailable(networkType)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d("NetworkCallback", "Network lost")
                previousNetworkType = null
                callback.onNetworkLost()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val networkType = getNetworkType(context)

                if (networkType != previousNetworkType && networkType != "No Connection") {
                    Log.d("NetworkCallback", "Network type changed: $previousNetworkType -> $networkType")
                    previousNetworkType = networkType
                    callback.onNetworkAvailable(networkType)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }

        return networkCallback
    }

    fun unregisterNetworkCallback(
        context: Context,
        callback: ConnectivityManager.NetworkCallback
    ) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(callback)
            previousNetworkType = null
            Log.d("NetworkCallback", "Network callback unregistered")
        } catch (e: Exception) {
            Log.e("NetworkCallback", "Error unregistering callback: ${e.message}")
        }
    }
}