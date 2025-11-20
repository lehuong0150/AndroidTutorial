package com.eco.musicplayer.audioplayer.music.models.contentprovider

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val starred: Boolean = false
)
