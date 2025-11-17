package com.eco.musicplayer.audioplayer.music.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

@Database(
    entities = [DrinkRecord::class],
    version = 1,
    exportSchema = false // theo doi lich su thay doi nhieu version khac
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drink_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}