package com.eco.musicplayer.audioplayer.music.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmsSenderService : Service() {
    companion object {
        const val ACTION_SEND = "ACTION_SEND"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_SECONDS_LEFT = "seconds_left"
        const val BROADCAST_ACTION = "com.eco.musicplayer.SMS_SENT"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SEND) {
            val phone = intent.getStringExtra(EXTRA_PHONE) ?: "Unknown"
            val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "No message"

            serviceScope.launch {
                for (i in 30 downTo 0) {
                    delay(1000)
                    sendBroadcast(Intent(BROADCAST_ACTION).apply {
                        putExtra(EXTRA_PHONE, phone)
                        putExtra(EXTRA_MESSAGE, message)
                        putExtra(EXTRA_SECONDS_LEFT, i)
                    })
                }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}