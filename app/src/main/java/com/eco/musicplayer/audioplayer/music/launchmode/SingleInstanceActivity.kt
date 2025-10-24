package com.eco.musicplayer.audioplayer.music.launchmode

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.MainActivity.Companion.instanceCount
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivitySingleInstanceBinding

/* Demo launchMode: singleInstance
    - Mỗi Activity chạy trong 1 task riêng, chỉ có 1 instance duy nhất.
    - Intent mới gọi sẽ kích hoạt onNewIntent(), không tạo Activity mới.
    - Khi mở lại app từ icon → quay về màn hình chính (vì task riêng biệt).
*/

class SingleInstanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleInstanceBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySingleInstanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logInstanceInfo()
        binding.btnClickInstance.setOnClickListener {
            val intent = Intent(this, SingleTaskActivity::class.java)
            startActivity(intent)
        }

        binding.btnOpenBrowser.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.google.com")
            }
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(
            "LaunchMode", "onNewIntent triggered for instance: " +
                    "${System.identityHashCode(this)}"
        )
    }

    private fun logInstanceInfo() {
        instanceCount++
        val instanceId = System.identityHashCode(this)
        Log.d("LaunchMode", "-----------------------------")
        Log.d("LaunchMode", "SingleInstance Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: $instanceCount")
    }
}