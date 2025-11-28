package com.eco.musicplayer.audioplayer.music.overlay

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityOverlayLockBinding

class OverlayLockActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityOverlayLockBinding.inflate(layoutInflater)
    }
    private lateinit var dots: List<View>
    private val correctPin = "1234"
    private var currentPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val appName = intent.getStringExtra("APP_NAME") ?: "App"
        binding.tvAppName.text = appName
        dots = listOf(
            binding.dot1,
            binding.dot2,
            binding.dot3,
            binding.dot4
        )
        setupKeypad()
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

            if (currentPin.length == 4) {
                checkPin()
            }
        }
    }

    private fun checkPin() {
        if (currentPin == correctPin) {
            Toast.makeText(this, "Mở khóa thành công!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "PIN sai! Thử lại", Toast.LENGTH_SHORT).show()
            currentPin = ""
            updateDots()
        }
    }
}