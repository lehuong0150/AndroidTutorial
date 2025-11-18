package com.eco.musicplayer.audioplayer.music.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

class DrinkWaterWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val dao = AppDatabase.getDatabase(context).drinkDao()
    override suspend fun doWork(): Result {
        dao.insert(DrinkRecord())
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channelId = "drink_channel"
        val notificationId = 999
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Nhac nho uong nuoc!!!!",
                NotificationManager.IMPORTANCE_HIGH
            )
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager)
                .createNotificationChannel(channel)
        }
        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle("Đã đến giờ uống nước rồi!!!")
            .setContentText("Uống ngay 100-200ml nước nào  ")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 300, 500))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Người dùng từ chối quyền
        }
    }
}