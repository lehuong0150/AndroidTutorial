package com.example.androidtutorial

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.MainActivity.Companion.instanceCount
import com.example.androidtutorial.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private val REQUEST_CODE_PICK_IMAGE = 100
    private val REQUEST_CODE_PERMISSION_STORAGE = 200

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        findViewById<android.view.View>(R.id.main).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                    )
                }
                insets
            }
        }

        logInstanceInfo()

        intent.getStringExtra("info_send")?.let {
            binding.edtReceive.setText(it)
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnUpdateImage.setOnClickListener { checkAndRequestPermission() }
    }

    private fun checkAndRequestPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_CODE_PERMISSION_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            grantResults.firstOrNull()?.let { result ->
                when (result) {
                    PackageManager.PERMISSION_GRANTED -> openGallery()
                    else -> showToast("Permission required to select photos!")
                }
            }
        }
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
