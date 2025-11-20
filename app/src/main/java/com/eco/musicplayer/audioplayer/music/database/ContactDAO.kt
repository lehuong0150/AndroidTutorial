package com.eco.musicplayer.audioplayer.music.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eco.musicplayer.audioplayer.music.models.contentprovider.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Insert
    suspend fun insertAll(contacts: List<Contact>)

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun getAllContactsBlocking(): List<Contact>

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactByIdBlocking(id: Long): Contact?
}