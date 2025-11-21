package com.eco.musicplayer.audioplayer.music.contentprovider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract
import com.eco.musicplayer.audioplayer.music.database.AppDatabase
import com.eco.musicplayer.audioplayer.music.models.contentprovider.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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

        private val CURSOR_COLUMNS = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.PHOTO_URI
        )
    }

    private lateinit var db: AppDatabase

    override fun onCreate(): Boolean {
        db = AppDatabase.getDatabase(context!!)
        return true
    }

    override fun getType(uri: Uri): String? = when (URI_MATCHER.match(uri)) {
        CONTACTS -> ContactsContract.Contacts.CONTENT_TYPE
        CONTACT_ID -> ContactsContract.Contacts.CONTENT_ITEM_TYPE
        else -> null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor = runBlocking(Dispatchers.IO) {
        val cursor = MatrixCursor(CURSOR_COLUMNS)

        when (URI_MATCHER.match(uri)) {
            CONTACTS -> {
                val contacts = db.contactDao().getAllContactsBlocking() // Dùng hàm blocking
                contacts.forEach { contact ->
                    cursor.addRow(
                        arrayOf(
                            contact.id,
                            contact.displayName,
                            if (contact.phoneNumber?.isNotEmpty() == true) 1 else 0,
                            if (contact.starred) 1 else 0,
                            contact.photoUri
                        )
                    )
                }
            }

            CONTACT_ID -> {
                val id = ContentUris.parseId(uri)
                val contact = db.contactDao().getContactByIdBlocking(id)
                contact?.let {
                    cursor.addRow(
                        arrayOf(
                            it.id,
                            it.displayName,
                            if (it.phoneNumber?.isNotEmpty() == true) 1 else 0,
                            if (it.starred) 1 else 0,
                            it.photoUri
                        )
                    )
                }
            }
        }

        // Đăng ký để nhận notify khi có thay đổi
        cursor.setNotificationUri(context!!.contentResolver, CONTENT_URI)
        cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (URI_MATCHER.match(uri) != CONTACTS) return null

        val displayName = values?.getAsString(ContactsContract.Contacts.DISPLAY_NAME)
            ?: return null

        val contact = Contact(
            displayName = displayName,
            phoneNumber = values.getAsString("phone_number"),
            email = values.getAsString("email"),
            photoUri = values.getAsString(ContactsContract.Contacts.PHOTO_URI),
            starred = values.getAsBoolean(ContactsContract.Contacts.STARRED) ?: false
        )

        val newId = runBlocking(Dispatchers.IO) {
            db.contactDao().insert(contact)
        }

        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return ContentUris.withAppendedId(CONTENT_URI, newId)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (URI_MATCHER.match(uri) != CONTACT_ID) return 0

        val id = ContentUris.parseId(uri)

        val deletedCount = runBlocking(Dispatchers.IO) {
            val contact = db.contactDao().getContactByIdBlocking(id) ?: return@runBlocking 0
            db.contactDao().delete(contact)
            1
        }

        if (deletedCount > 0) {
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
        }
        return deletedCount
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (URI_MATCHER.match(uri) != CONTACT_ID) return 0

        val id = ContentUris.parseId(uri)

        val updatedCount = runBlocking(Dispatchers.IO) {
            val oldContact = db.contactDao().getContactByIdBlocking(id) ?: return@runBlocking 0

            val updatedContact = oldContact.copy(
                displayName = values?.getAsString(ContactsContract.Contacts.DISPLAY_NAME)
                    ?: oldContact.displayName,
                phoneNumber = values?.getAsString("phone_number") ?: oldContact.phoneNumber,
                email = values?.getAsString("email") ?: oldContact.email,
                photoUri = values?.getAsString(ContactsContract.Contacts.PHOTO_URI)
                    ?: oldContact.photoUri,
                starred = values?.getAsBoolean(ContactsContract.Contacts.STARRED)
                    ?: oldContact.starred
            )

            db.contactDao().update(updatedContact)
            1
        }

        if (updatedCount > 0) {
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
        }
        return updatedCount
    }
}