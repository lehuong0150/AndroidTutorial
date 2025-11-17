import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

class DrinkWaterWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "drink_channel"
        private const val NOTIFICATION_ID = 999
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    override suspend fun doWork(): Result {
        // Lưu record
        AppDatabase.getDatabase(applicationContext).drinkDao().insert(DrinkRecord())

        // Hiện thông báo
        createNotificationChannel()
        showNotification()

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc uống nước",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc uống nước mỗi 30 phút"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return  // Không có quyền → không hiện
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_new)
            .setContentTitle("Đã đến giờ uống nước rồi!!!")
            .setContentText("Uống ngay 1 cốc nước đi nào")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}