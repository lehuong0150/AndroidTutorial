package com.example.androidtutorial.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil(private val context: Context) {

    companion object {
        const val REQUEST_CODE_PERMISSION_STORAGE = 200
    }

    fun checkPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            onDenied()
        }
    }

    fun requestPermission(activity: Activity) {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            REQUEST_CODE_PERMISSION_STORAGE
        )
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            grantResults.firstOrNull()?.let { result ->
                when (result) {
                    PackageManager.PERMISSION_GRANTED -> onGranted()
                    else -> onDenied()
                }
            }
        }
    }
}