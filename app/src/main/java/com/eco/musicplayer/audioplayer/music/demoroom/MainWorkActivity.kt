package com.eco.musicplayer.audioplayer.music.demoroom

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainWorkBinding
import com.eco.musicplayer.audioplayer.music.permission.PermissionUtil
import com.eco.musicplayer.audioplayer.music.viewmodel.WorkViewModel

class MainWorkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainWorkBinding
    private val viewModel: WorkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.scheduleDrinkReminder(this)

        // bat dau len lich
        viewModel.scheduleDrinkReminder(this)

        //quan sat so coc nuoc da uong
        viewModel.drinkCount.observe(this) { count ->
            binding.tvCount.text = getString(R.string.number_water_drink, "$count")
        }

        binding.btnDrinkNow.setOnClickListener {
            viewModel.drinkNow()
        }

        //ds lich su
        viewModel.drinkRecords.observe(this) { list ->
            Log.d("DrinkApp", "Tong ban ghi: ${list.size}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionUtil(this).handlePermissionResult(
            requestCode = requestCode,
            grantResults = grantResults,
            onGranted = {
                // Quyền được cấp → lên lịch lại ngay
                viewModel.scheduleDrinkReminder(this)
            },
            onDenied = {
                Toast.makeText(this, "Vui lòng bật thông báo trong Cài đặt", Toast.LENGTH_LONG).show()
            }
        )
    }

}