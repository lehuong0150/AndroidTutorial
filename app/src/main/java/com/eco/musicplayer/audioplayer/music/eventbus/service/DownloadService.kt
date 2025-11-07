package com.eco.musicplayer.audioplayer.music.eventbus.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.eco.musicplayer.audioplayer.music.models.event.DownloadCompleteEvent
import com.eco.musicplayer.audioplayer.music.models.event.DownloadProgressEvent
import com.eco.musicplayer.audioplayer.music.models.event.StartDownloadEvent
import com.eco.musicplayer.audioplayer.music.models.event.StopDownloadEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeDownloads = mutableMapOf<Int, Job>()
    private var downloadCounter = 0
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        //Huy tat ca Download
        serviceScope.cancel()

        EventBus.getDefault().unregister(this)
    }

    //Nhan lenh bat dau download tu Activity
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onStartDownload(event: StartDownloadEvent) {
        val downloadId = ++downloadCounter
        //tao download job
        val job = serviceScope.launch {
            try {
                //gia lap download
                for (progress in 0..100 step 10) {
                    delay(500)
                    //Gui progress
                    EventBus.getDefault()
                        .post(DownloadProgressEvent(downloadId, progress, event.fileName))
                }
                EventBus.getDefault().post(
                    DownloadCompleteEvent(
                        downloadId, event.fileName, true, "Download completed successfully"
                    )
                )
            } catch (e: Exception) {
                EventBus.getDefault().post(
                    DownloadCompleteEvent(
                        downloadId, event.fileName, false, "Download failed: ${e.message}"
                    )
                )
//                finally {
//                    activeDownloads.remove(downloadId)
//                }
            }

            // activeDownloads[downloadId] = job
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onStopDownload(event: StopDownloadEvent) {
        activeDownloads[event.downloadId]?.cancel()
        activeDownloads.remove(event.downloadId)
    }
}