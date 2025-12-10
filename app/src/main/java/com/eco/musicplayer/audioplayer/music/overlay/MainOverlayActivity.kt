package com.eco.musicplayer.audioplayer.music.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainOverLayBinding

class MainOverlayActivity : AppCompatActivity() {
    private val TAG = "MainOverlayActivity"

    private val binding by lazy {
        ActivityMainOverLayBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        Log.d(TAG, "onCreate - App started")

        binding.btStartService.setOnClickListener {
            Log.d(TAG, "Click Start Service")
            if (checkPermission()) {
                Log.d(TAG, "Co quyen overlay")
                if (checkUsageStatsPermission()) {
                    Log.d(TAG, "Co quyen Usage Stats - Start service")
                    startLockService()
                } else {
                    Log.d(TAG, "Chua co quyen Usage Stats")
                    requestUsageStatsPermission()
                }
            } else {
                Log.d(TAG, "Chua co quyen overlay - Yeu cau quyen")
                requestOverlayPermission()
            }
        }

        binding.btStopService.setOnClickListener {
            Log.d(TAG, "Click Stop Service")
            stopLockService()
        }

        // Long press để thêm app test
        binding.btStartService.setOnLongClickListener {
            Log.d(TAG, "Long Click Start - Add Test Apps")
            addTestApp()
            checkLockedAppsList()
            true
        }

        // Long press Stop để mở Settings thủ công
        binding.btStopService.setOnLongClickListener {
            Log.d(TAG, "Long Click Stop - Manual Settings")
            testPermissionManually()
            true
        }

        checkPermissionOnStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Cap nhat UI")
        checkPermissionOnStart()
    }

    private fun checkPermissionOnStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermission = Settings.canDrawOverlays(this)
            Log.d(TAG, "====================================")
            Log.d(TAG, "Kiem tra quyen overlay: $hasPermission")
            Log.d(TAG, "====================================")

            if (!hasPermission) {
                binding.btStartService.text = "Cap quyen de bat dau"
            } else {
                binding.btStartService.text = "Bat dau AppLock"
            }
        } else {
            binding.btStartService.text = "Bat dau AppLock"
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val hasPermission = Settings.canDrawOverlays(this)
                Log.d(TAG, "====================================")
                Log.d(TAG, "checkPermission: $hasPermission")
                Log.d(TAG, "Android Version: ${Build.VERSION.SDK_INT}")
                Log.d(TAG, "Package Name: $packageName")
                Log.d(TAG, "Context: ${this.javaClass.simpleName}")

                val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
                if (appOpsManager != null) {
                    val mode = appOpsManager.checkOpNoThrow(
                        "android:system_alert_window",
                        android.os.Process.myUid(),
                        packageName
                    )
                    Log.d(TAG, "AppOpsManager mode: $mode (0=ALLOWED, 1=IGNORED, 2=ERRORED)")
                }

                Log.d(TAG, "====================================")
                hasPermission
            } catch (e: Exception) {
                Log.e(TAG, "Loi check permission: ${e.message}", e)
                false
            }
        } else {
            Log.d(TAG, "Android < M, khong can quyen overlay")
            true
        }
    }

    private fun requestOverlayPermission() {
        Log.d(TAG, "requestOverlayPermission called")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                AlertDialog.Builder(this)
                    .setTitle("Can cap quyen")
                    .setMessage("AppLock can quyen 'Hien thi tren man hinh' de hoat dong.\n\n" +
                            "LUU Y:\n" +
                            "1. Trong man hinh tiep theo, TIM APP CUA BAN (co the phai cuon xuong)\n" +
                            "2. CLICK vao ten app\n" +
                            "3. BAT cong tac 'Allow display...'\n" +
                            "4. Nhan BACK de quay lai")
                    .setPositiveButton("Di den Cai dat") { _, _ ->
                        openOverlaySettings()
                    }
                    .setNegativeButton("Huy", null)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "Loi request permission: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun openOverlaySettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Mo Settings overlay permission")
                Log.d(TAG, "Package: $packageName")

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    Log.d(TAG, "Da mo Settings voi package cu the")
                } else {
                    Log.d(TAG, "Khong mo duoc Settings cu the, thu tong quat")
                    val generalIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivity(generalIntent)
                    Log.d(TAG, "Da mo Settings tong quat")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Loi mo Settings: ${e.message}", e)

            showDialog("Huong dan cap quyen",
                "Vui long lam theo cac buoc:\n\n" +
                        "1. Vao Cai dat (Settings)\n" +
                        "2. Chon Ung dung (Apps)\n" +
                        "3. Tim app nay trong danh sach\n" +
                        "4. Chon 'Quyen dac biet' hoac 'Nang cao'\n" +
                        "5. Chon 'Hien thi tren cac ung dung khac'\n" +
                        "6. BAT quyen nay")
        }
    }

    private fun startLockService() {
        try {
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
            Log.d(TAG, "Service started successfully")
            showDialog("Da bat dau", "AppLock dang chay")
        } catch (e: Exception) {
            Log.e(TAG, "Loi start service: ${e.message}")
            e.printStackTrace()
            showDialog("Loi", "Khong the khoi dong service: ${e.message}")
        }
    }

    private fun stopLockService() {
        try {
            val intent = Intent(this, OverlayService::class.java)
            stopService(intent)
            Log.d(TAG, "Service stopped successfully")
            showDialog("Da dung", "AppLock da dung")
        } catch (e: Exception) {
            Log.e(TAG, "Loi stop service: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun testPermissionManually() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)

            showDialog("Huong dan",
                "1. Tim 'Special app access' hoac 'Advanced'\n" +
                        "2. Chon 'Display over other apps'\n" +
                        "3. BAT quyen\n" +
                        "4. Quay lai app")
        } catch (e: Exception) {
            Log.e(TAG, "Loi mo app settings: ${e.message}")
            showDialog("Loi", "Khong the mo Settings: ${e.message}")
        }
    }

    private fun checkUsageStatsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
                val granted = mode == android.app.AppOpsManager.MODE_ALLOWED
                Log.d(TAG, "Usage Stats permission: $granted")
                return granted
            } catch (e: Exception) {
                Log.e(TAG, "Error checking usage stats: ${e.message}")
            }
        }
        return false
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("Can them quyen")
            .setMessage("AppLock can quyen 'Truy cap du lieu su dung' de phat hien app nao dang chay.\n\n" +
                    "Trong man hinh tiep theo:\n" +
                    "1. Tim app cua ban\n" +
                    "2. BAT quyen 'Permit usage access'\n" +
                    "3. Quay lai app")
            .setPositiveButton("Di den Cai dat") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } catch (e: Exception) {
                    Log.e(TAG, "Khong the mo Usage Settings: ${e.message}")
                }
            }
            .setNegativeButton("Huy", null)
            .show()
    }

    private fun addTestApp() {
        val prefs = getSharedPreferences("AppLock", MODE_PRIVATE)
        val editor = prefs.edit()

        val testApps = listOf(
            "com.sina.weibo" to "Weibo",
            "com.tencent.mm" to "WeChat",
            "com.tencent.mobileqq" to "QQ",
            "com.tencent.qqlive" to "Tencent Video",
            "tv.danmaku.bili" to "Bilibili",
            "com.ss.android.ugc.aweme" to "Douyin/TikTok",
            "com.taobao.taobao" to "Taobao",
            "com.tmall.wireless" to "Tmall",
            "com.jd.lib.android.main" to "JD",
            "com.baidu.searchbox" to "Baidu",
            "com.android.chrome" to "Chrome",
            "com.google.android.youtube" to "YouTube",
            "com.facebook.katana" to "Facebook",
            "com.instagram.android" to "Instagram"
        )

        val savedApps = mutableSetOf<String>()
        testApps.forEach { (packageName, appName) ->
            savedApps.add(packageName)
            editor.putString("app_name_$packageName", appName)
            editor.putBoolean("is_locked_$packageName", true)
        }

        editor.putStringSet("locked_apps", savedApps)
        editor.apply()

        Log.d(TAG, "Added test apps: $testApps")
        Log.d(TAG, "Total apps added: ${testApps.size}")
    }

    private fun checkLockedAppsList() {
        val prefs = getSharedPreferences("AppLock", MODE_PRIVATE)
        val lockedApps = prefs.getStringSet("locked_apps", emptySet()) ?: emptySet()

        Log.d(TAG, "========================================")
        Log.d(TAG, "DANH SACH APP DA KHOA:")
        Log.d(TAG, "Tong so: ${lockedApps.size}")

        if (lockedApps.isEmpty()) {
            Log.e(TAG, "DANH SACH TRONG!")
            showDialog("Loi", "Khong them duoc app vao danh sach!")
        } else {
            val appList = StringBuilder()
            lockedApps.forEach { packageName ->
                val appName = prefs.getString("app_name_$packageName", packageName)
                val isLocked = prefs.getBoolean("is_locked_$packageName", false)
                Log.d(TAG, "- $appName ($packageName) - Locked: $isLocked")
                appList.append("$appName\n")
            }
            Log.d(TAG, "========================================")

            showDialog("Da them ${lockedApps.size} app", appList.toString().trim())
        }
    }
}