import android.database.ContentObserver
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.contentprovider.ContactsProvider
import com.eco.musicplayer.audioplayer.music.databinding.ActivityObserveBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObserveActivity : AppCompatActivity() {

    private val binding by lazy { ActivityObserveBinding.inflate(layoutInflater) }

    private val scope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    private var updateCount = 0

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            updateCount++
            loadContactsSummary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupWindowInsets()

        contentResolver.registerContentObserver(
            ContactsProvider.CONTENT_URI,
            true,
            contentObserver
        )

        loadContactsSummary()

        Toast.makeText(
            this,
            getString(R.string.observe_tracking),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }

    private fun loadContactsSummary() {
        scope.launch(Dispatchers.IO) {
            try {
                val contacts = mutableListOf<String>()
                var starredCount = 0

                val cursor: Cursor? = contentResolver.query(
                    ContactsProvider.CONTENT_URI,
                    null,
                    null,
                    null,
                    "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
                )

                cursor?.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val starredIndex = it.getColumnIndex(ContactsContract.Contacts.STARRED)

                    while (it.moveToNext()) {
                        val name = it.getString(nameIndex)
                        val starred = it.getInt(starredIndex) == 1

                        if (starred) starredCount++

                        val prefix = if (starred) "⭐" else "👤"
                        contacts.add("$prefix $name")
                    }
                }

                withContext(Dispatchers.Main) {
                    updateUI(contacts.size, starredCount, contacts)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ObserveActivity,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateUI(totalCount: Int, starredCount: Int, contacts: List<String>) {

        binding.tvContactCount.text =
            getString(R.string.observe_total_contacts, totalCount, starredCount)

        binding.tvContactList.text = if (contacts.isEmpty()) {
            getString(R.string.observe_no_contacts)
        } else {
            contacts.joinToString("\n")
        }

        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        binding.tvLastUpdate.text =
            getString(R.string.observe_update_time, time, updateCount)
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
        scope.cancel()

        Toast.makeText(
            this,
            getString(R.string.observe_stop_tracking),
            Toast.LENGTH_SHORT
        ).show()
    }
}
