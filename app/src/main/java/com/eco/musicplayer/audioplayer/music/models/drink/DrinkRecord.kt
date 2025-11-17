package com.eco.musicplayer.audioplayer.music.models.drink

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_records")
data class DrinkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
