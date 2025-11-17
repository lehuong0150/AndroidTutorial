package com.eco.musicplayer.audioplayer.music.viewmodel

import DrinkWaterWorker
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord
import com.eco.musicplayer.audioplayer.music.permission.PermissionUtil
import com.eco.musicplayer.audioplayer.music.repository.DrinkRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<Application>()

    private val database by lazy { AppDatabase.getDatabase(app) }
    private val repo by lazy { DrinkRepository(database.drinkDao()) }

    val drinkRecords: LiveData<List<DrinkRecord>> by lazy { repo.allRecord }
    val drinkCount: LiveData<Int> = liveData { emit(repo.getCount()) }

    fun drinkNow() {
        viewModelScope.launch {
            repo.insert()
        }
    }

    fun scheduleDrinkReminder(context: Context) {
        val permissionUtil = PermissionUtil(context)

        permissionUtil.checkNotificationPermission(
            onGranted = {
                val workRequest = OneTimeWorkRequestBuilder<DrinkWaterWorker>()
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        "drink_water_reminder",
                        ExistingWorkPolicy.REPLACE,
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