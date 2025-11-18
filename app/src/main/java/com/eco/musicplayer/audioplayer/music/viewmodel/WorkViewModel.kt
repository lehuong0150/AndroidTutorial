package com.eco.musicplayer.audioplayer.music.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord
import com.eco.musicplayer.audioplayer.music.permission.PermissionUtil
import com.eco.musicplayer.audioplayer.music.repository.DrinkRepository
import com.eco.musicplayer.audioplayer.music.worker.DrinkWaterWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkViewModel(application: Application) : AndroidViewModel(application) {

    private val repo by lazy { DrinkRepository(AppDatabase.getDatabase(application).drinkDao()) }
    val drinkRecords: LiveData<List<DrinkRecord>> = repo.allRecord

    val drinkCount: LiveData<Int> = repo.getTodayCountFlow().asLiveData()

    fun drinkNow() {
        viewModelScope.launch {
            repo.insert()
        }
    }

    fun scheduleDrinkReminder(context: Context) {
        val permissionUtil = PermissionUtil(context)

        permissionUtil.checkNotificationPermission(
            onGranted = {
                val workRequest = PeriodicWorkRequestBuilder<DrinkWaterWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        "drink_water_reminder",
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                    )
            },
            onDenied = {
                if (context is Activity) {
                    permissionUtil.requestNotificationPermission(context)
                }
            }
        )
    }
}