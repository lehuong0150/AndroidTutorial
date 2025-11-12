package com.eco.musicplayer.audioplayer.music.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.eco.musicplayer.audioplayer.music.R
import kotlinx.coroutines.*
import java.io.File

class DownloadService : Service() {

    companion object {
        private const val ACTION_STOP = "ACTION_STOP_DOWNLOAD"
        private const val NOTIF_ID = 100
        private const val CHANNEL_ID = "download_channel"
    }

    private var downloadJob: Job? = null
    private val stopPendingIntent by lazy { createStopPendingIntent() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            cancelDownloadAndStop()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification(0))
        startDownload()
        return START_STICKY
    }

    private fun startDownload() {
        val fileName = "Groovy_Vibe_Downloaded.mp3"
        val destFile = File(getExternalFilesDir(null), fileName)

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                resources.openRawResource(R.raw.do_an).use { input ->
                    destFile.outputStream().use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalRead = 0L
                        val fileSize = input.available().toLong()

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (!isActive) {
                                destFile.delete()
                                return@launch
                            }
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            val progress =
                                if (fileSize > 0) (totalRead * 100 / fileSize).toInt() else 0
                            updateNotification(progress, "$fileName - $progress%")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    showCompleteNotification(fileName)
                    stopSelf()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateNotification(-1, "Lỗi: ${e.message}")
                }
            }
        }
    }

    private fun buildNotification(
        progress: Int,
        contentText: String = "Groovy Vibe - $progress%"
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang tải nhạc")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress.coerceAtMost(100), progress <= 0)
            .setOngoing(true)
            .addAction(R.drawable.ic_delete, "Dừng", stopPendingIntent)
            .build()
    }

    private fun updateNotification(progress: Int, text: String) {
        val notification = buildNotification(progress, text)
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, notification)
    }

    private fun showCompleteNotification(fileName: String) {
        val noti = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tải xong!")
            .setContentText("$fileName đã lưu vào bộ nhớ")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID + 1, noti)
    }

    private fun createStopPendingIntent(): PendingIntent {
        val intent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelDownloadAndStop() {
        downloadJob?.cancel()
        File(getExternalFilesDir(null), "Groovy_Vibe_Downloaded.mp3").delete()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        downloadJob?.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID, "Tải nhạc",
                    NotificationManager.IMPORTANCE_LOW
                )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}