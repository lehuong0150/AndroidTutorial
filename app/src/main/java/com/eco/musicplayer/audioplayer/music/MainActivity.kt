package com.eco.musicplayer.audioplayer.music

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
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
import com.eco.musicplayer.audioplayer.music.models.modelActivity.ActivityResult
import com.eco.musicplayer.audioplayer.music.models.modelActivity.BundleData
import com.eco.musicplayer.audioplayer.music.utils.NavigationEvent
import com.eco.musicplayer.audioplayer.music.viewmodel.MainViewModel

/*Demo tổng hợp về:
* Vòng đời của Activity (Lifecycle):
*     - Quan sát các callback như onCreate, onStart, onResume, onPause, onStop, onDestroy.
*     - Nút "Finish", "Rotation", "Share" minh họa quá trình kết thúc, xoay màn hình,
*       và Activity bị tạm dừng khi có Intent khác mở chồng lên.
*
* Giao tiếp giữa các Activity:
*     - Gửi dữ liệu qua Intent, Bundle (chuyen du lieu qua StandardActivity).
*     - Nhận dữ liệu trả về từ SecondActivity bằng ActivityResultContracts.
*
* Các chế độ Launch Mode trong Android:
*     - Nút "Standard" → mở StandardActivity.
*     - Nút "SingleTop" → mở SingleTopActivity.
*     - Nút "SingleTask" → mở SingleTaskActivity.
*     - Nút "SingleInstance" → mở SingleInstanceActivity.
*     - Nút "Flag" → mở bat ki mot loai launch mode.
*
* Lưu và khôi phục trạng thái:
*     - Dữ liệu trong EditText được lưu lại khi xoay màn hình (onSaveInstanceState / onRestoreInstanceState).
*/
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    companion object {
        var instanceCount = 0
    }

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

        viewModel.logInstanceCreation(taskId)

        setupObservers()
        setupLifecycleButtons()
        setupNavigationButtons()
        setupLaunchModeButtons()
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
            viewModel.onFinishClicked()
            finish()
        }

        binding.btnRotation.setOnClickListener {
            Log.d("LifecycleMainActivity", "Configuration changes (Rotation)")
            viewModel.onRotationClicked()
            toggleScreenOrientation()
        }

        binding.btnShare.setOnClickListenerDebounced {
            Log.d("LifecycleMainActivity", "App is paused by the system (Share intent)")
            viewModel.onShareClicked()
            shareText("App is paused by the system")
        }
    }

    private fun setupNavigationButtons() {
        binding.btnSecondActivity.setOnClickListener {
            Log.d("LifecycleMainActivity", "Navigating to SecondActivity")
            viewModel.onSecondActivityClicked()

            Intent(this, SecondActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("info_send", binding.edtSend.text.toString())
            }.also { intent ->
                getResultFromSecondActivity.launch(intent)
            }
        }
    }

    private fun setupLaunchModeButtons() {
        binding.btnStandard.setOnClickListenerDebounced {
            Log.d("LaunchMode", "Opening StandardActivity")
            viewModel.onLaunchModeClicked("Standard", taskId)
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
                val bundleData = BundleData(
                    taskId = event.taskId,
                    instanceLabel = "MainActivity Instance #${event.instanceCount}",
                    startTime = System.currentTimeMillis(),
                    isForeground = true
                )

                val bundle = Bundle().apply {
                    putInt("task_id", bundleData.taskId)
                    putString("instance_label", bundleData.instanceLabel)
                    putLong("start_time", bundleData.startTime)
                    putBoolean("is_foreground", bundleData.isForeground)
                }

                startActivity(Intent(this, StandardActivity::class.java).apply {
                    putExtras(bundle)
                })
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
                    "SingleTask" -> intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    "SingleInstance" -> intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    )
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
            viewModel.updateEditTextContent(text)
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
        viewModel.onActivityDestroy()
        Log.d(
            "LifecycleMainActivity", "onDestroy - Instance: " +
                    "${System.identityHashCode(this)}"
        )
    }
}