package com.eco.musicplayer.audioplayer.music.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.eco.musicplayer.audioplayer.music.R

class MusicBoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var binder = MusicBinder()

    inner class MusicBinder : Binder() {
        fun getService() = this@MusicBoundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicBoundService", "onCreate")
        mediaPlayer = MediaPlayer.create(this, R.raw.groovy_vibe)
        mediaPlayer?.isLooping = true
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    override fun onDestroy() {
        Log.d("MusicBoundService", "onDestroy")
        mediaPlayer?.release()
        super.onDestroy()
    }
}