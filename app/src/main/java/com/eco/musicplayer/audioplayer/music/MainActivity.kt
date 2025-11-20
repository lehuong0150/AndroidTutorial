package com.eco.musicplayer.audioplayer.music

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.databinding.ActivityMainBinding
import com.eco.musicplayer.audioplayer.music.launchmode.IntentFlagActivity
import com.eco.musicplayer.audioplayer.music.launchmode.SingleInstanceActivity
import com.eco.musicplayer.audioplayer.music.launchmode.SingleTaskActivity
import com.eco.musicplayer.audioplayer.music.launchmode.SingleTopActivity
import com.eco.musicplayer.audioplayer.music.launchmode.StandardActivity
import com.eco.musicplayer.audioplayer.music.models.modelactivity.ActivityResult
import com.eco.musicplayer.audioplayer.music.models.modelactivity.NetworkStateCallback
import com.eco.musicplayer.audioplayer.music.permission.PermissionUtil
import com.eco.musicplayer.audioplayer.music.utils.NavigationEvent
import com.eco.musicplayer.audioplayer.music.utils.NetworkUtils
import com.eco.musicplayer.audioplayer.music.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private lateinit var permissionUtil: PermissionUtil
    private val getResultFromSecondActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.takeIf { it.resultCode == RESULT_OK }?.data?.let { data ->
                data.getStringExtra("info_send_result")?.let { message ->
                    val activityResult = ActivityResult(message)
                    viewModel.onActivityResult(activityResult)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).let { systemBars ->
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
            insets
        }
        permissionUtil = PermissionUtil(this)
        viewModel.logInstanceCreation(taskId)
        registerNetworkMonitoring()
        checkNetworkConnection()
        checkAndRequestPermission()
        setupObservers()
        setupLifecycleButtons()
        setupNavigationButtons()
        setupLaunchModeButtons()
    }

    private fun checkAndRequestPermission() {
        permissionUtil.checkNotificationPermission(
            onGranted = {
                showToast("Quyền thông báo đã được cấp")
            },
            onDenied = {
                permissionUtil.requestNotificationPermission(this)
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtil.REQUEST_CODE_PERMISSION_NOTIFICATION) {
            permissionUtil.handlePermissionResult(
                requestCode = requestCode,
                grantResults = grantResults,
                onGranted = {
                    showToast("Đã cấp quyền thông báo")
                },
                onDenied = {
                    showToast("Quyền thông báo bị từ chối!")
                }
            )
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            if (binding.edtSend.text.toString() != state.editTextContent) {
                binding.edtSend.setText(state.editTextContent)
            }

            state.toastMessage?.let { message ->
                showToast(message)
                viewModel.clearToastMessage()
            }
        }

        viewModel.navigationEvent.observe(this) { event ->
            event?.let {
                handleNavigationEvent(it)
                viewModel.clearNavigationEvent()
            }
        }
    }

    private fun setupLifecycleButtons() {
        binding.btnFinish.setOnClickListener {
            Log.d("LifecycleMainActivity", "App finished and restarted")
            finish()
        }

        binding.btnRotation.setOnClickListener {
            Log.d("LifecycleMainActivity", "Configuration changes (Rotation)")
            toggleScreenOrientation()
        }

        binding.btnShare.setOnClickListenerDebounced {
            Log.d("LifecycleMainActivity", "App is paused by the system (Share intent)")
            if (viewModel.onShareClicked()) {
                shareText("App is paused by the system")
            }
        }
    }

    private fun registerNetworkMonitoring() {
        networkCallback = NetworkUtils.registerNetworkCallback(
            context = this,
            callback = object : NetworkStateCallback {
                override fun onNetworkAvailable(networkType: String) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Đã kết nối: $networkType",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("NetworkMonitor", "Network connected: $networkType")
                    }
                }

                override fun onNetworkLost() {
                    runOnUiThread {
                        showNetworkLostDialog()
                    }
                }
            }
        )
    }

    private fun showNetworkLostDialog() {
        AlertDialog.Builder(this)
            .setTitle("Mất kết nối mạng")
            .setMessage("Kết nối internet đã bị ngắt. Vui lòng kiểm tra lại.")
            .setPositiveButton("Thử lại") { _, _ ->
                checkNetworkConnection()
            }
            .setNegativeButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()

        Log.w("NetworkMonitor", "Network connection lost")
    }

    private fun checkNetworkConnection() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            val networkType = NetworkUtils.getNetworkType(this)
            Log.d("NetworkCheck", "Network available: $networkType")
        } else {
            showNetworkErrorDialog()
        }
    }

    private fun showNetworkErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Không có kết nối mạng")
            .setMessage("Vui lòng kiểm tra kết nối internet và thử lại.")
            .setPositiveButton("Thử lại") { _, _ ->
                checkNetworkConnection()
            }
            .setNegativeButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()

        Log.w("NetworkCheck", "No network connection available")
    }

    private fun setupNavigationButtons() {
        binding.btnSecondActivity.setOnClickListener {
            Log.d("LifecycleMainActivity", "Navigating to SecondActivity")

            Intent(this, SecondActivity::class.java).apply {
                putExtra("info_send", binding.edtSend.text.toString())
            }.also { intent ->
                getResultFromSecondActivity.launch(intent)
            }
        }
    }

    private fun setupLaunchModeButtons() {
        binding.btnParcelable.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Demo Parcelable")
            viewModel.onParcelableClicked(taskId)
        }

        binding.btnSerializable.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Demo Serializable")
            viewModel.onSerializableClicked(taskId)
        }

        binding.btnBundle.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Demo Bundle Manual")
            viewModel.onBundleManualClicked(taskId)
        }

        binding.btnSingleTop.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Opening SingleTopActivity")
            viewModel.onLaunchModeClicked("SingleTop", taskId)
        }

        binding.btnSingleTask.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Opening SingleTaskActivity")
            viewModel.onLaunchModeClicked("SingleTask", taskId)
        }

        binding.btnSingleInstance.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Opening SingleInstanceActivity")
            viewModel.onLaunchModeClicked("SingleInstance", taskId)
        }

        binding.btnIntentFlag.setOnClickListenerDebounced {
            showLaunchModeDialog()
        }
    }

    private fun showLaunchModeDialog() {
        val launchModes = arrayOf("Standard", "SingleTop", "SingleTask", "SingleInstance")

        AlertDialog.Builder(this)
            .setTitle("Select Launch Mode to demo: ")
            .setItems(launchModes) { _, which ->
                viewModel.onIntentFlagSelected(launchModes[which])
            }
            .show()
    }

    private fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.OpenStandardActivity -> {
                val intent = Intent(this, StandardActivity::class.java).apply {
                    putExtra("BUNDLE_DATA", event.bundleData as Parcelable)
                }
                startActivity(intent)
            }

            is NavigationEvent.OpenSingleTopActivity -> {
                startActivity(Intent(this, SingleTopActivity::class.java))
            }

            is NavigationEvent.OpenSingleTaskActivity -> {
                startActivity(Intent(this, SingleTaskActivity::class.java))
            }

            is NavigationEvent.OpenSingleInstanceActivity -> {
                startActivity(Intent(this, SingleInstanceActivity::class.java))
            }

            is NavigationEvent.OpenIntentFlagActivity -> {
                val intent = Intent(this, IntentFlagActivity::class.java)

                when (event.mode) {
                    "SingleTop" -> intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    "SingleTask" -> intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Sửa: dùng NEW_TASK
                    "SingleInstance" -> intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Sửa: bỏ MULTIPLE_TASK
                    "Standard" -> { /* Không cần flag */
                    }
                }

                Log.d("LaunchMode", "Opening ${event.mode} | flags=${intent.flags}")
                intent.putExtra("launch_mode", event.mode)
                startActivity(intent)
            }
        }
    }

    private fun toggleScreenOrientation() {
        requestedOrientation = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun shareText(message: String) {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }.let { intent ->
            startActivity(Intent.createChooser(intent, "Share text"))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("EDIT_TEXT_CONTENT", binding.edtSend.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getString("EDIT_TEXT_CONTENT")?.let { text ->
            viewModel.restoreEditTextContent(text)
        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        Log.d(
//            "LaunchMode", "onNewIntent triggered for instance: " +
//                    "${System.identityHashCode(this)}"
//        )
//    }

    override fun onStart() {
        super.onStart()
        Log.d("LifecycleMainActivity", "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LifecycleMainActivity", "onRestart")
    }

    override fun onResume() {
        super.onResume()
        viewModel.onShareDialogDismissed()
        Log.d("LifecycleMainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifecycleMainActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifecycleMainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let { callback ->
            NetworkUtils.unregisterNetworkCallback(this, callback)
        }
        Log.d(
            "LifecycleMainActivity", "onDestroy - Instance: " +
                    "${System.identityHashCode(this)}"
        )
    }
}