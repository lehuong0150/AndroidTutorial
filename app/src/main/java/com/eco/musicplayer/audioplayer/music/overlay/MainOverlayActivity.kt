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
                Log.d(TAG, "✓ Có quyền overlay")
                // Kiểm tra quyền Usage Stats
                if (checkUsageStatsPermission()) {
                    Log.d(TAG, "✓ Có quyền Usage Stats → Start service")
                    startLockService()
                } else {
                    Log.d(TAG, "✗ Chưa có quyền Usage Stats")
                    requestUsageStatsPermission()
                }
            } else {
                Log.d(TAG, "✗ Chưa có quyền overlay → Yêu cầu quyền")
                requestOverlayPermission()
            }
        }

        binding.btStopService.setOnClickListener {
            Log.d(TAG, "Click Stop Service")
            stopLockService()
        }

        // BUTTON TEST - Long press để thêm app test
        binding.btStartService.setOnLongClickListener {
            Log.d(TAG, "Long Click Start - Add Test Apps")
            addTestApp()
            true
        }

        // Long press Stop để mở Settings thủ công
        binding.btStopService.setOnLongClickListener {
            Log.d(TAG, "Long Click Stop - Manual Settings")
            testPermissionManually()
            true
        }

        // Kiểm tra quyền khi mở app
        checkPermissionOnStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Cập nhật UI")
        // Cập nhật lại UI mỗi khi quay lại activity
        checkPermissionOnStart()
    }

    private fun checkPermissionOnStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermission = Settings.canDrawOverlays(this)
            Log.d(TAG, "====================================")
            Log.d(TAG, "Kiểm tra quyền overlay: $hasPermission")
            Log.d(TAG, "====================================")

            if (!hasPermission) {
                binding.btStartService.text = "Cấp quyền để bắt đầu"
            } else {
                binding.btStartService.text = "Bắt đầu AppLock"
            }
        } else {
            binding.btStartService.text = "Bắt đầu AppLock"
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

                // Thử cách khác để check
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
                Log.e(TAG, "Lỗi check permission: ${e.message}", e)
                false
            }
        } else {
            Log.d(TAG, "Android < M, không cần quyền overlay")
            true
        }
    }

    private fun requestOverlayPermission() {
        Log.d(TAG, "requestOverlayPermission called")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                // Hiển thị dialog giải thích CHI TIẾT
                AlertDialog.Builder(this)
                    .setTitle("Cần cấp quyền")
                    .setMessage("AppLock cần quyền 'Hiển thị trên màn hình' để hoạt động.\n\n" +
                            "⚠️ LƯU Ý:\n" +
                            "1. Trong màn hình tiếp theo, TÌM APP CỦA BẠN (có thể phải cuộn xuống)\n" +
                            "2. CLICK vào tên app\n" +
                            "3. BẬT công tắc 'Allow display...'\n" +
                            "4. Nhấn BACK để quay lại")
                    .setPositiveButton("Đi đến Cài đặt") { _, _ ->
                        openOverlaySettings()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi request permission: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun openOverlaySettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Mở Settings overlay permission")
                Log.d(TAG, "Package: $packageName")

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

                // Check xem intent có thể mở được không
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    Log.d(TAG, "✓ Đã mở Settings với package cụ thể")
                } else {
                    // Thử mở settings tổng quát
                    Log.d(TAG, "Không mở được Settings cụ thể, thử tổng quát")
                    val generalIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivity(generalIntent)
                    Log.d(TAG, "✓ Đã mở Settings tổng quát")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi mở Settings: ${e.message}", e)

            // Hướng dẫn user vào thủ công
            showDialog("Hướng dẫn cấp quyền",
                "Vui lòng làm theo các bước:\n\n" +
                        "1. Vào Cài đặt (Settings)\n" +
                        "2. Chọn Ứng dụng (Apps)\n" +
                        "3. Tìm app này trong danh sách\n" +
                        "4. Chọn 'Quyền đặc biệt' hoặc 'Nâng cao'\n" +
                        "5. Chọn 'Hiển thị trên các ứng dụng khác'\n" +
                        "6. BẬT quyền này")
        }
    }

    private fun startLockService() {
        try {
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
            Log.d(TAG, "Service started successfully")
            showDialog("Đã bắt đầu", "AppLock đang chạy")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi start service: ${e.message}")
            e.printStackTrace()
            showDialog("Lỗi", "Không thể khởi động service: ${e.message}")
        }
    }

    private fun stopLockService() {
        try {
            val intent = Intent(this, OverlayService::class.java)
            stopService(intent)
            Log.d(TAG, "Service stopped successfully")
            showDialog("Đã dừng", "AppLock đã dừng")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi stop service: ${e.message}")
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

    // FUNCTION TEST - Mở Settings app thủ công
    private fun testPermissionManually() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)

            showDialog("Hướng dẫn",
                "1. Tìm 'Special app access' hoặc 'Advanced'\n" +
                        "2. Chọn 'Display over other apps'\n" +
                        "3. BẬT quyền\n" +
                        "4. Quay lại app")
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi mở app settings: ${e.message}")
            showDialog("Lỗi", "Không thể mở Settings: ${e.message}")
        }
    }

    // Kiểm tra quyền Usage Stats
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

    // Yêu cầu quyền Usage Stats
    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("Cần thêm quyền")
            .setMessage("AppLock cần quyền 'Truy cập dữ liệu sử dụng' để phát hiện app nào đang chạy.\n\n" +
                    "Trong màn hình tiếp theo:\n" +
                    "1. Tìm app của bạn\n" +
                    "2. BẬT quyền 'Permit usage access'\n" +
                    "3. Quay lại app")
            .setPositiveButton("Đi đến Cài đặt") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } catch (e: Exception) {
                    Log.e(TAG, "Không thể mở Usage Settings: ${e.message}")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Thêm app vào danh sách khóa để test
    private fun addTestApp() {
        val prefs = getSharedPreferences("AppLock", MODE_PRIVATE)
        val editor = prefs.edit()

        // Thêm các app Trung Quốc phổ biến để test
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
            "com.baidu.searchbox" to "Baidu"
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
        showDialog("Test Mode", "Đã thêm ${testApps.size} app Trung Quốc để test:\n" +
                testApps.joinToString("\n") { it.second })
    }
}