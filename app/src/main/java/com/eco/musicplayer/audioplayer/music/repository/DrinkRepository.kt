package com.eco.musicplayer.audioplayer.music.repository

import androidx.lifecycle.LiveData
import com.eco.musicplayer.audioplayer.music.database.DrinkDAO
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord
import kotlinx.coroutines.flow.Flow

class DrinkRepository(private val dao: DrinkDAO) {
    val allRecord: LiveData<List<DrinkRecord>> =
        dao.getAllRecords()

    fun getTodayCountFlow(): Flow<Int> = dao.getTodayCountFlow()
    suspend fun insert() {
        dao.insert(DrinkRecord())
    }
}