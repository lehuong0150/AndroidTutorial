package com.eco.musicplayer.audioplayer.music.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eco.musicplayer.audioplayer.music.models.contentprovider.Contact
import com.eco.musicplayer.audioplayer.music.models.drink.DrinkRecord

@Database(
    entities = [
        DrinkRecord::class,
        Contact::class,           // danh bạ
//        Song::class,              // nhạc
//        CallLogEntry::class,      // nhật ký cuộc gọi
//        CalendarEvent::class,     // lịch
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drinkDao(): DrinkDAO
    abstract fun contactDao(): ContactDAO
//    abstract fun songDao(): SongDao
//    abstract fun callLogDao(): CallLogDao
//    abstract fun calendarDao(): CalendarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eco_music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}