package com.example.androidtutorial

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.MainActivity.Companion.instanceCount
import com.example.androidtutorial.databinding.ActivitySecondBinding
import com.example.androidtutorial.permission.PermissionUtil

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private lateinit var permissionUtil: PermissionUtil
    private val REQUEST_CODE_PICK_IMAGE = 100

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<android.view.View>(R.id.main).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                }
                insets
            }
        }
        permissionUtil = PermissionUtil(this)

        logInstanceInfo()

        intent.getStringExtra("info_send")?.let {
            binding.edtReceive.setText(it)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnUpdateImage.setOnClickListener { checkAndRequestPermission() }
    }

    private fun checkAndRequestPermission() {
        permissionUtil.checkPermission(
            onGranted = { openGallery() },
            onDenied = { permissionUtil.requestPermission(this) }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtil.handlePermissionResult(
            requestCode = requestCode,
            grantResults = grantResults,
            onGranted = { openGallery() },
            onDenied = { showToast("Permission required to select photos!") }
        )
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { selectedImageUri ->
                binding.imgView.setImageURI(selectedImageUri)
                showToast("Photo update successful!")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("LifecycleSecondActivity", "onNewIntent")
        Log.d("LaunchMode", "onNewIntent triggered for instance: ${System.identityHashCode(this)}")
    }

    override fun onStart() {
        super.onStart()
        Log.d("LifecycleSecondActivity", "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LifecycleSecondActivity", "onRestart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LifecycleSecondActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifecycleSecondActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifecycleSecondActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifecycleSecondActivity", "onDestroy")
    }

    override fun finish() {
        Intent().apply {
            putExtra("info_send_result", binding.edtReceive.text.toString())
        }.also { data ->
            setResult(RESULT_OK, data)
        }
        super.finish()
    }

    private fun logInstanceInfo() {
        instanceCount++
        val instanceId = System.identityHashCode(this)

        Log.d("LaunchMode", "-----------------------------")
        Log.d("LaunchMode", "SingleTop Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: $instanceCount")
    }
}
