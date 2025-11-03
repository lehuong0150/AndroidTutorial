package com.eco.musicplayer.audioplayer.music.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil(private val context: Context) {

    companion object {
        const val REQUEST_CODE_PERMISSION_STORAGE = 200
        private const val MAX_DENY_COUNT = 2
    }

    private val permission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    private val prefs = context.getSharedPreferences("perm_prefs", Context.MODE_PRIVATE)

    fun checkPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            onDenied()
        }
    }

    fun requestPermission(activity: Activity) {
        val denyCount = prefs.getInt("deny_count", 0)

        if (denyCount >= MAX_DENY_COUNT) {
            openAppSettings(activity)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                REQUEST_CODE_PERMISSION_STORAGE
            )
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                prefs.edit().putInt("deny_count", 0).apply()
                onGranted()
            } else {
                prefs.edit().putInt("deny_count", prefs.getInt("deny_count", 0) + 1).apply()
                onDenied()
            }
        }
    }

    private fun openAppSettings(activity: Activity) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }.also { activity.startActivity(it) }
    }
}