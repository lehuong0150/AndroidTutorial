package com.eco.musicplayer.audioplayer.music.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainServiceBinding

/**
 * MÀN HÌNH DEMO 4 LOẠI SERVICE TRONG ANDROID (8-16)
 * 1. Foreground Service (MusicService)     → Phát nhạc khi thoát app -> stopSelf
 * 2. Foreground Service (DownloadService)  → Tải file + notification -> stopService
 * 3. Background Service (SmsSenderService)    → Background task, sống độc lập
 * 4. Bound Service (MusicBoundService)     → Điều khiển realtime, chết khi Activity chết
 */
class MainServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainServiceBinding
    private lateinit var rotateAnim: Animation
    private var musicBoundService: MusicBoundService? = null
    private var isMusicBound = false

    private val musicBoundConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("BOUND_SERVICE", "onServiceConnected()")
            musicBoundService = (service as MusicBoundService.MusicBinder).getService()
            isMusicBound = true
            updateBoundButtons(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("BOUND_SERVICE", "onServiceDisconnected()")
            isMusicBound = false
            musicBoundService = null
        }
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.takeIf { it.action == SmsSenderService.BROADCAST_ACTION }?.let {
                val secondsLeft = it.getIntExtra(SmsSenderService.EXTRA_SECONDS_LEFT, -1)
                val phone = it.getStringExtra(SmsSenderService.EXTRA_PHONE).orEmpty()
                val message = it.getStringExtra(SmsSenderService.EXTRA_MESSAGE).orEmpty()

                binding.tvStatusBackground.text = if (secondsLeft > 0) {
                    getString(R.string.sms_counting, secondsLeft, phone, message)
                } else {
                    showToast(R.string.service_completed)
                    getString(R.string.sms_result, phone, message)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_album)
        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            btnClose.setOnClickListener { finish() }

            // Foreground Service - MusicService
            btnPlay.setOnClickListener {
                controlMusic(MusicService.ACTION_PLAY, R.string.status_music_play, true)
            }
            btnPause.setOnClickListener {
                controlMusic(MusicService.ACTION_PAUSE, R.string.status_music_pause, false)
            }
            btnStop.setOnClickListener {
                controlMusic(MusicService.ACTION_STOP, R.string.status_music_stop, false)
            }

            // Foreground Service - DownloadService
            btnDownload.setOnClickListener {
                startForegroundServiceCompat<DownloadService>()
                showToast("Bắt đầu tải nhạc...")
            }

            // Background Service
            btnSendSms.setOnClickListener {
                Intent(this@MainServiceActivity, SmsSenderService::class.java).apply {
                    action = SmsSenderService.ACTION_SEND
                    putExtra(SmsSenderService.EXTRA_PHONE, "0819486150")
                    putExtra(SmsSenderService.EXTRA_MESSAGE, "Hello từ Background Service!")
                }.also { startService(it) }

                tvStatusBackground.text = getString(R.string.status_sms)
                showToast("Đã khởi chạy Background Service!")
            }

            // Bound Service
            btnPlayBound.setOnClickListener {
                controlBoundMusic({ play() }, R.string.status_music_play, true)
            }
            btnPauseBound.setOnClickListener {
                controlBoundMusic({ pause() }, R.string.status_music_pause, false)
            }
            btnStopBound.setOnClickListener {
                controlBoundMusic({ stop() }, R.string.status_music_stop, false)
            }
        }
    }

    private fun controlMusic(action: String, statusResId: Int, startAnim: Boolean) {
        startService(Intent(this, MusicService::class.java).apply {
            this.action = action
        })
        updateAnimation(startAnim)
        binding.tvStatus.text = getString(statusResId)
    }

    private fun controlBoundMusic(
        action: MusicBoundService.() -> Unit,
        statusResId: Int,
        startAnim: Boolean
    ) {
        if (isMusicBound) {
            musicBoundService?.action()
            updateAnimation(startAnim)
            binding.tvstatusBound.text = getString(statusResId)
        } else {
            showToast("Chưa kết nối Bound Service!")
        }
    }

    private fun updateAnimation(start: Boolean) {
        if (start) binding.imgCover.startAnimation(rotateAnim)
        else binding.imgCover.clearAnimation()
    }

    private fun updateBoundButtons(enabled: Boolean) {
        binding.apply {
            btnPlayBound.isEnabled = enabled
            btnPauseBound.isEnabled = enabled
            btnStopBound.isEnabled = enabled
        }
    }

    private inline fun <reified T> startForegroundServiceCompat() {
        val intent = Intent(this, T::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        registerSmsReceiver()
        bindMusicService()
    }

    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(smsReceiver) }
        unbindMusicService()
    }

    override fun onResume() {
        super.onResume()
        if (!isMusicBound) bindMusicService()
    }

    private fun registerSmsReceiver() {
        val filter = IntentFilter(SmsSenderService.BROADCAST_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(smsReceiver, filter)
        }
    }

    private fun bindMusicService() {
        Intent(this, MusicBoundService::class.java).also {
            bindService(it, musicBoundConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindMusicService() {
        if (isMusicBound) {
            Log.w("BOUND_SERVICE", "unbindService()")
            unbindService(musicBoundConnection)
            isMusicBound = false
            updateBoundButtons(false)
        }
    }
}