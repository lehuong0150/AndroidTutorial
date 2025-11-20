package com.eco.musicplayer.audioplayer.music.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.room.Room
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import kotlinx.coroutines.flow.forEach

class ContactsProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.eco.contacts.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/contacts")
        private const val CONTACTS = 1
        private const val CONTACT_ID = 2

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "contacts", CONTACTS)
            addURI(AUTHORITY, "contacts/#", CONTACT_ID)
        }
    }

    private lateinit var db: AppDatabase
    override fun onCreate(): Boolean {
        db = Room.databaseBuilder(context!!, AppDatabase::class.java, "contacts.db")
            .allowMainThreadQueries()
            .build()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = MatrixCursor(
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.PHOTO_URI
            )
        )
        when (URI_MATCHER.match(uri)) {
//            CONTACTS -> {
//                val contracts = db.contactDao().getAllContacts()
//                contracts.forEach {contract ->
//                    cursor.addRow(
//                        arrayOf(
//                            contract.
//                        )
//                    )
//                }
//            }
        }
        return cursor
    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("Not yet implemented")
    }
}