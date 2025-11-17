package com.eco.musicplayer.audioplayer.music.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.eco.musicplayer.audioplayer.music.R
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

class DownloadService : Service() {

    companion object {
        private const val NOTIF_ID = 100
        private const val CHANNEL_ID = "download_channel"
        private const val BUFFER_SIZE = 8 * 1024
        private const val TAG = "DownloadService"

        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
    }

    private var downloadJob: Job? = null
    private var isPaused = false
    private var totalRead = 0L
    private var fileSize = 0L

    private val fileName = "Groovy_Vibe_Downloaded.mp3"
    private val destFile by lazy { File(getExternalFilesDir(null), fileName) }
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        when (intent?.action) {
            ACTION_PAUSE -> pauseDownload()
            ACTION_RESUME -> resumeDownload()
            else -> {
                startForegroundSafe()
                startDownload()
            }
        }

        return START_STICKY
    }

    private fun startForegroundSafe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIF_ID, buildNotification())
        }
    }

    private fun startDownload() {
        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                resources.openRawResource(R.raw.do_an).use { input ->
                    destFile.outputStream().use { output ->
                        fileSize = input.available().toLong()
                        downloadWithProgress(input, output)
                    }
                }
                onDownloadComplete()
            }.onFailure {
                onDownloadError(it)
            }
        }
    }

    private suspend fun downloadWithProgress(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)

        while (coroutineContext.isActive) {
            waitWhilePaused()
            if (!coroutineContext.isActive) break

            val bytesRead = input.read(buffer)
            if (bytesRead == -1) break

            output.write(buffer, 0, bytesRead)
            totalRead += bytesRead

            updateProgress()
        }
    }

    private suspend fun waitWhilePaused() {
        while (isPaused && coroutineContext.isActive) {
            updateProgress()
            delay(100)
        }
    }

    private suspend fun updateProgress() {
        val progress = calculateProgress()
        withContext(Dispatchers.Main) {
            updateNotification(progress)
        }
    }

    private fun calculateProgress(): Int =
        if (fileSize > 0) (totalRead * 100 / fileSize).toInt() else 0

    private suspend fun onDownloadComplete() {
        withContext(Dispatchers.Main) {
            showCompleteNotification()
            stopSelf()
        }
    }

    private suspend fun onDownloadError(error: Throwable) {
        withContext(Dispatchers.Main) {
            updateNotification(-1, "Lỗi: ${error.message}")
            stopSelf()
        }
    }

    private fun pauseDownload() {
        isPaused = true
    }

    private fun resumeDownload() {
        isPaused = false
    }

    private fun buildNotification(
        progress: Int = 0,
        contentText: String = "$fileName - $progress%"
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isPaused) "Tạm dừng tải" else "Đang tải nhạc")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress.coerceAtMost(100), progress < 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .apply { addNotificationActions(this) }
            .build()
    }

    private fun addNotificationActions(builder: NotificationCompat.Builder) {
        if (isPaused) {
            builder.addAction(R.drawable.ic_play, "Tiếp tục", createActionIntent(ACTION_RESUME))
        } else {
            builder.addAction(R.drawable.ic_pause, "Dừng", createActionIntent(ACTION_PAUSE))
        }
        builder.addAction(R.drawable.ic_delete, "Hủy", createCancelIntent())
    }

    private fun updateNotification(
        progress: Int,
        text: String = "$fileName - $progress%"
    ) {
        notificationManager.notify(NOTIF_ID, buildNotification(progress, text))
    }

    private fun showCompleteNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tải xong!")
            .setContentText("$fileName đã lưu vào bộ nhớ")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIF_ID + 1, notification)
    }

    private fun createActionIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            action.hashCode(),
            Intent(this, DownloadService::class.java).apply { this.action = action },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createCancelIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            this,
            0,
            Intent(DownloadStopReceiver.ACTION_CANCEL_DOWNLOAD).apply { setPackage(packageName) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tải nhạc",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo tiến trình tải nhạc"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        downloadJob?.cancel()
        destFile.takeIf { it.exists() }?.delete()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}