package com.eco.musicplayer.audioplayer.music.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.eco.musicplayer.audioplayer.music.MainActivity
import com.eco.musicplayer.audioplayer.music.R

class MusicService : Service() {
    private lateinit var player: MediaPlayer
    private val NOTIF_ID = 1
    private val CHANNED_ID = "music_channel"

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        var isPlaying = false
    }

    override fun onCreate() {
        Log.d("MusicService", "onCreate")
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.groovy_vibe)
        player.isLooping = true
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand")
        when (intent?.action) {
            ACTION_PLAY -> {
                if (!isPlaying) {
                    player.start()
                    isPlaying = true
                    updateNotification("Dang Phat", "Nhac", true)
                }
            }

            ACTION_PAUSE -> {
                if (isPlaying) {
                    player.pause()
                    isPlaying = false
                    updateNotification("Da tam dung", "Nhan Play de tiep tuc!!", true)
                }
            }

            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun updateNotification(title: String, text: String, isPlayingNow: Boolean) {
        val playPauseIntent = Intent(this, MusicService::class.java).apply {
            action = if (isPlayingNow) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePending = PendingIntent.getService(
            this,
            0,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            this,
            2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val collapsedView = RemoteViews(packageName, R.layout.layout_music_notification).apply {
            setTextViewText(R.id.tvTitle, title)
            setTextViewText(R.id.tvStatus, text)
            setImageViewResource(R.id.imgAlbum, R.drawable.img_album_cover)
            setImageViewResource(
                R.id.btnPlayPause, if (isPlayingNow) R.drawable.ic_pause else R.drawable.ic_play
            )
            setOnClickPendingIntent(R.id.btnPlayPause, playPausePending)
            setOnClickPendingIntent(R.id.btnStop, stopPending)
        }

        val notification =
            NotificationCompat.Builder(this, CHANNED_ID).setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(openPending).setOngoing(true).setCustomContentView(collapsedView)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()).apply {
                    // Android 14: BẮT BUỘC khai báo type
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    }
                }.build()

        //android 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNED_ID, "Music", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null
    override fun onDestroy() {
        Log.d("MusicService", "onDestroy")
        player.stop()
        player.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}