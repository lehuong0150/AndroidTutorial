package com.eco.musicplayer.audioplayer.music.overlay

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class OverlayService : Service() {

    private val TAG = "OverlayService"
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // Check mỗi 1 giây
    private var lastPackageName = ""
    private var lastCheckTime = 0L

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkCurrentApp()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "========================================")
        Log.d(TAG, "Service created")
        Log.d(TAG, "========================================")
        handler.post(checkRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "========================================")
        Log.d(TAG, "Service started")
        Log.d(TAG, "========================================")
        return START_STICKY
    }

    private fun checkCurrentApp() {
        try {
            val currentPackage = getCurrentForegroundApp()
            val currentTime = System.currentTimeMillis()

            Log.d(TAG, "Checking... Current: $currentPackage | Last: $lastPackageName")

            if (currentPackage != lastPackageName || currentTime - lastCheckTime > 3000) {
                lastPackageName = currentPackage
                lastCheckTime = currentTime

                Log.d(TAG, "Current foreground app: $currentPackage")

                if (currentPackage.isNotEmpty()) {
                    val prefs = getSharedPreferences("AppLock", Context.MODE_PRIVATE)
                    val isLocked = prefs.getBoolean("is_locked_$currentPackage", false)

                    Log.d(TAG, "App $currentPackage - Locked: $isLocked")

                    if (isLocked) {
                        val appName =
                            prefs.getString("app_name_$currentPackage", currentPackage)
                                ?: currentPackage

                        Log.d(TAG, "Showing lock screen for: $appName")
                        showLockScreen(appName, currentPackage)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkCurrentApp: ${e.message}", e)
        }
    }

    private fun getCurrentForegroundApp(): String {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager =
                    getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

                if (usageStatsManager == null) {
                    Log.e(TAG, "UsageStatsManager is null")
                    return ""
                }

                val currentTime = System.currentTimeMillis()

                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    currentTime - 1000 * 60, // 1 phút gần nhất
                    currentTime
                )

                if (stats.isNullOrEmpty()) {
                    Log.e(TAG, "UsageStats empty - missing PACKAGE_USAGE_STATS permission")
                    return ""
                }

                val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
                Log.d(TAG, "Found ${stats.size} apps in usage stats")

                sortedStats.take(3).forEachIndexed { index, stat ->
                    Log.d(
                        TAG,
                        "${index + 1}. ${stat.packageName} - Last used: ${currentTime - stat.lastTimeUsed}ms ago"
                    )
                }

                val mostRecentApp = sortedStats.firstOrNull() ?: return ""

                val packageName = mostRecentApp.packageName

                if (packageName == this.packageName) {
                    Log.d(TAG, "Skip self app: $packageName")
                    return ""
                }

                if (packageName.contains("launcher") || packageName.contains("home")) {
                    Log.d(TAG, "Skip launcher: $packageName")
                    return ""
                }

                Log.d(TAG, "Most recent app: $packageName")
                return packageName
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting foreground app: ${e.message}", e)
        }

        return ""
    }

    private fun showLockScreen(appName: String, packageName: String) {
        try {
            val intent = Intent(this, OverlayLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("APP_NAME", appName)
                putExtra("PACKAGE_NAME", packageName)
            }
            startActivity(intent)
            Log.d(TAG, "Lock screen shown for $appName")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing lock screen: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        handler.removeCallbacks(checkRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
