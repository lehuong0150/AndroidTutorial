package com.eco.musicplayer.audioplayer.music.service

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainServiceBinding

class MainServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainServiceBinding
    private lateinit var rotateAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_album)
        binding.btnPlay.setOnClickListener {
            startService(Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_PLAY
            })
            binding.imgCover.startAnimation(rotateAnim)
            binding.tvStatus.text = "Đang phát nhạc..."
        }

        binding.btnPause.setOnClickListener {
            startService(Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_PAUSE
            })
            binding.imgCover.clearAnimation()
            binding.tvStatus.text = "Đã tạm dừng!"
        }

        binding.btnStop.setOnClickListener {
            startService(Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_STOP
            })
            binding.imgCover.clearAnimation()
            binding.tvStatus.text = "Đã dừng hẳn"
        }

        binding.btnDownload.setOnClickListener {
            val intent = Intent(this, DownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Bắt đầu tải nhạc...", Toast.LENGTH_SHORT).show()
        }
    }
}
