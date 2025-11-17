package com.eco.musicplayer.audioplayer.music.repository

import androidx.lifecycle.LiveData
import com.eco.musicplayer.audioplayer.music.database.DrinkDAO
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

class DrinkRepository(private val dao: DrinkDAO) {
    val allRecord: LiveData<List<DrinkRecord>> = dao.getAll()
    suspend fun insert() = dao.insert(DrinkRecord())
    suspend fun getCount() = dao.getCount()
}