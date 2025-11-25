package com.eco.musicplayer.audioplayer.music.democontent

import android.content.ContentUris
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eco.musicplayer.audioplayer.music.contentprovider.ContactsProvider
import com.eco.musicplayer.audioplayer.music.databinding.ActivityContactBinding
import com.eco.musicplayer.audioplayer.music.models.contentprovider.Contact
import kotlinx.coroutines.*

class ContactActivity : AppCompatActivity() {

    private val binding by lazy { ActivityContactBinding.inflate(layoutInflater) }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ContentObserver
    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadContacts()
            Toast.makeText(this@ContactActivity, "Dữ liệu đã được cập nhật!", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
        setupRecyclerView()
        setupListeners()

        contentResolver.registerContentObserver(
            ContactsProvider.CONTENT_URI,
            true,
            contentObserver
        )

        loadContacts()
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter(
            onStarClick = { contact -> toggleStar(contact) },
            onDeleteClick = { contact -> deleteContact(contact) }
        )

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@ContactActivity)
            adapter = contactAdapter
        }
    }

    private fun setupListeners() {
        binding.btnAddContact.setOnClickListener { addContact() }
    }

    private fun addContact() {
        val name = binding.etDisplayName.text?.toString()?.trim()
        val phone = binding.etPhoneNumber.text?.toString()?.trim()
        val email = binding.etEmail.text?.toString()?.trim()

        if (name.isNullOrEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên hiển thị", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val values = ContentValues().apply {
                    put(ContactsContract.Contacts.DISPLAY_NAME, name)
                    put("phone_number", phone)
                    put("email", email)
                    put(ContactsContract.Contacts.STARRED, false)
                }

                val uri = contentResolver.insert(ContactsProvider.CONTENT_URI, values)

                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        Toast.makeText(this@ContactActivity, "Đã thêm contact: $name", Toast.LENGTH_SHORT).show()
                        clearInputs()
                    } else {
                        Toast.makeText(this@ContactActivity, "Lỗi khi thêm contact", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContactActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadContacts() {
        scope.launch(Dispatchers.IO) {
            try {
                val contacts = mutableListOf<Contact>()

                val cursor: Cursor? = contentResolver.query(
                    ContactsProvider.CONTENT_URI,
                    null, null, null,
                    "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
                )

                cursor?.use {
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val starIndex = it.getColumnIndex(ContactsContract.Contacts.STARRED)

                    while (it.moveToNext()) {
                        contacts.add(
                            Contact(
                                id = it.getLong(idIndex),
                                displayName = it.getString(nameIndex),
                                phoneNumber = null,
                                email = null,
                                starred = it.getInt(starIndex) == 1
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    contactAdapter.submitList(contacts)
                    updateEmptyState(contacts.isEmpty())
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContactActivity, "Lỗi khi tải: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleStar(contact: Contact) {
        scope.launch(Dispatchers.IO) {
            try {
                val uri = ContentUris.withAppendedId(ContactsProvider.CONTENT_URI, contact.id)
                val values = ContentValues().apply {
                    put(ContactsContract.Contacts.STARRED, !contact.starred)
                }

                val updated = contentResolver.update(uri, values, null, null)

                if (updated > 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ContactActivity,
                            if (!contact.starred) "Đã đánh dấu sao" else "☆ Đã bỏ đánh dấu sao",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContactActivity, "Lỗi khi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteContact(contact: Contact) {
        scope.launch(Dispatchers.IO) {
            try {
                val uri = ContentUris.withAppendedId(ContactsProvider.CONTENT_URI, contact.id)
                val deleted = contentResolver.delete(uri, null, null)

                if (deleted > 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ContactActivity, "Đã xóa ${contact.displayName}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ContactActivity, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearInputs() {
        binding.etDisplayName.text?.clear()
        binding.etPhoneNumber.text?.clear()
        binding.etEmail.text?.clear()
    }

    private fun updateEmptyState(empty: Boolean) {
        binding.rvContacts.visibility = if (empty) android.view.View.GONE else android.view.View.VISIBLE
        binding.tvEmptyState.visibility = if (empty) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
        scope.cancel()
    }
}
