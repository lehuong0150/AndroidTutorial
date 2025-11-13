package com.eco.musicplayer.audioplayer.music.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadStopReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_CANCEL_DOWNLOAD  = "com.eco.musicplayer.STOP_DOWNLOAD"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_CANCEL_DOWNLOAD ) {
            val serviceIntent = Intent(context, DownloadService::class.java)
            context?.stopService(serviceIntent)
        }
    }
}