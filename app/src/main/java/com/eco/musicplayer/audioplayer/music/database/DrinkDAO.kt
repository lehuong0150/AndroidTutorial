package com.eco.musicplayer.audioplayer.music.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

@Dao
interface DrinkDAO {
    @Insert
    suspend fun insert(drinkRecord: DrinkRecord)

    @Query("SELECT COUNT(*) FROM drink_records")
    suspend fun getCount(): Int

    @Query("SELECT * FROM drink_records ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<DrinkRecord>>
}