package com.eco.musicplayer.audioplayer.music.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDAO {
    @Query("SELECT * FROM drink_records ORDER BY timestamp DESC")
    fun getAllRecords(): LiveData<List<DrinkRecord>>

    @Query("SELECT COUNT(*) FROM drink_records")
    fun getCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: DrinkRecord)

    @Query(
        """SELECT COUNT(*) FROM drink_records 
            WHERE date(timestamp / 1000, 'unixepoch') = date('now')"""
    )
    fun getTodayCountFlow(): Flow<Int>
}