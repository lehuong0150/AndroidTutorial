package com.eco.musicplayer.audioplayer.music.overlay

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityOverlayLockBinding

class OverlayLockActivity : AppCompatActivity() {
    private val TAG = "OverlayLockActivity"

    private val binding by lazy {
        ActivityOverlayLockBinding.inflate(layoutInflater)
    }
    private lateinit var dots: List<View>
    private val correctPin = "1234"
    private var currentPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "========================================")
        Log.d(TAG, "OVERLAY LOCK ACTIVITY - onCreate")
        Log.d(TAG, "========================================")

        // Thiết lập window flags TRƯỚC khi setContentView
        setupWindowFlags()

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val appName = intent.getStringExtra("APP_NAME") ?: "App"
        val packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""

        Log.d(TAG, "Khoa app: $appName ($packageName)")

        binding.tvAppName.text = "$appName dang bi khoa"

        dots = listOf(
            binding.dot1,
            binding.dot2,
            binding.dot3,
            binding.dot4
        )

        setupKeypad()
        blockBackButton()

        Log.d(TAG, "Setup hoan tat")
    }

    private fun setupWindowFlags() {
        window.apply {
            // Hiển thị trên lock screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }

            // Các flags bổ sung
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            // Đảm bảo activity hiển thị trên cùng
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                attributes = attributes.apply {
                    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
            }
        }
    }

    private fun blockBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@OverlayLockActivity,
                    "Nhap PIN de mo khoa!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "User nhan Back button - Da chan")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Activity hien thi")
        currentPin = ""
        updateDots()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun setupKeypad() {
        val buttonIds = listOf(
            binding.bt1, binding.bt2, binding.bt3,
            binding.bt4, binding.bt5, binding.bt6,
            binding.bt7, binding.bt8, binding.bt9,
            binding.bt0
        )

        buttonIds.forEachIndexed { index, button ->
            button.setOnClickListener {
                val number = if (index == 9) "0" else "${index + 1}"
                onNumberClick(number)
            }
        }

        binding.btnDelete.setOnClickListener {
            onDeleteClick()
        }
    }

    private fun onDeleteClick() {
        if (currentPin.isNotEmpty()) {
            currentPin = currentPin.dropLast(1)
            updateDots()
            Log.d(TAG, "Xoa so - Pin hien tai: ${currentPin.length} so")
        }
    }

    private fun updateDots() {
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < currentPin.length)
                    R.drawable.pin_dot_filled
                else
                    R.drawable.pin_dot_empty
            )
        }
    }

    private fun onNumberClick(number: String) {
        if (currentPin.length < 4) {
            currentPin += number
            updateDots()
            Log.d(TAG, "Nhap so: $number - Pin hien tai: ${currentPin.length}/4")

            if (currentPin.length == 4) {
                checkPin()
            }
        }
    }

    private fun checkPin() {
        Log.d(TAG, "Kiem tra PIN...")
        if (currentPin == correctPin) {
            Log.d(TAG, "PIN DUNG - Mo khoa thanh cong")
            Toast.makeText(this, "Mo khoa thanh cong!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Log.d(TAG, "PIN SAI - Nhap lai")
            Toast.makeText(this, "PIN sai! Thu lai (PIN: 1234)", Toast.LENGTH_SHORT).show()
            currentPin = ""
            updateDots()
        }
    }
}