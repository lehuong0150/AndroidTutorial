package com.eco.musicplayer.audioplayer.music.overlay

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log

class OverlayService : Service() {

    private val TAG = "OverlayService"
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L
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
        Log.d(TAG, "SERVICE CREATED")

        val hasUsagePermission = checkUsageStatsPermission()
        Log.d(TAG, "Usage Stats Permission: $hasUsagePermission")

        if (!hasUsagePermission) {
            Log.e(TAG, "KHONG CO QUYEN USAGE STATS!")
            Log.e(TAG, "Service se KHONG hoat dong duoc!")
        }

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

            Log.d(TAG, "==========================================")
            Log.d(TAG, "Checking... Current: $currentPackage")
            Log.d(TAG, "Last: $lastPackageName")
            Log.d(TAG, "==========================================")

            if (currentPackage != lastPackageName || currentTime - lastCheckTime > 3000) {
                lastPackageName = currentPackage
                lastCheckTime = currentTime

                if (currentPackage.isNotEmpty()) {
                    val prefs = getSharedPreferences("AppLock", Context.MODE_PRIVATE)

                    val allLockedApps = prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
                    Log.d(TAG, "Danh sach khoa co ${allLockedApps.size} app")

                    val isLocked = prefs.getBoolean("is_locked_$currentPackage", false)
                    val isInList = allLockedApps.contains(currentPackage)

                    Log.d(TAG, "Kiem tra: $currentPackage")
                    Log.d(TAG, "   - In list: $isInList")
                    Log.d(TAG, "   - Is locked: $isLocked")

                    if (isLocked && isInList) {
                        val appName = prefs.getString("app_name_$currentPackage", currentPackage)
                            ?: currentPackage
                        Log.d(TAG, "KHOA APP: $appName")
                        showLockScreen(appName, currentPackage)
                    } else {
                        Log.d(TAG, "App khong bi khoa hoac khong trong danh sach")
                    }
                } else {
                    Log.w(TAG, "Khong lay duoc package name cua app hien tai")
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
                    currentTime - 1000 * 60,
                    currentTime
                )

                if (stats.isNullOrEmpty()) {
                    Log.e(TAG, "UsageStats EMPTY - CHUA CAP QUYEN!")
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
            Log.d(TAG, "========================================")
            Log.d(TAG, "BAT DAU HIEN THI MAN HINH KHOA")
            Log.d(TAG, "App: $appName ($packageName)")

            // Kiểm tra quyền overlay
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Log.e(TAG, "KHONG CO QUYEN OVERLAY!")
                    Log.e(TAG, "========================================")
                    return
                }
            }

            val intent = Intent(this, OverlayLockActivity::class.java).apply {
                // Các flags quan trọng
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                putExtra("APP_NAME", appName)
                putExtra("PACKAGE_NAME", packageName)
            }

            startActivity(intent)
            Log.d(TAG, "DA GOI startActivity() THANH CONG")
            Log.d(TAG, "========================================")

        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "LOI HIEN THI MAN HINH KHOA: ${e.message}", e)
            Log.e(TAG, "Stack trace:")
            e.printStackTrace()
            Log.e(TAG, "========================================")
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permission: ${e.message}")
                false
            }
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        handler.removeCallbacks(checkRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}