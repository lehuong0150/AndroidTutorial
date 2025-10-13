package com.example.androidtutorial

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.databinding.ActivityMainBinding
import com.example.androidtutorial.launchmode.IntentFlagActivity
import com.example.androidtutorial.launchmode.SingleInstanceActivity
import com.example.androidtutorial.launchmode.SingleTaskActivity
import com.example.androidtutorial.launchmode.SingleTopActivity
import com.example.androidtutorial.launchmode.StandardActivity
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

    companion object {
        var instanceCount = 0
    }

    private var choose: String? = null

    private val getResultFromSecondActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.takeIf { it.resultCode == RESULT_OK }?.data?.let { data ->
                data.getStringExtra("info_send_result")?.let { message ->
                    binding.edtSend.setText(message)
                    showToast(message)
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

        logInstanceInfo()
        setupLifecycleButtons(savedInstanceState)
        setupNavigationButtons(savedInstanceState)
        setupLaunchModeButtons()
    }

    private fun logInstanceInfo() {
        instanceCount++
        val instanceId = System.identityHashCode(this)

        Log.d("LaunchMode", "-----------------------------")
        Log.d("LaunchMode", "SingleTop Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: $instanceCount")
    }

    private fun setupLifecycleButtons(savedInstanceState: Bundle?) {
        choose = savedInstanceState?.getString("0")

        binding.btnFinish.setOnClickListener {
            Log.d("LifecycleMainActivity", "App finished and restarted")
            savedInstanceState?.getString("Finish")?.also { choose = it }
            finish()
        }

        binding.btnRotation.setOnClickListener {
            Log.d("LifecycleMainActivity", "Configuration changes (Rotation)")
            savedInstanceState?.getString("Rotation")?.also { choose = it }
            toggleScreenOrientation()
        }

        binding.btnShare.setOnClickListener {
            Log.d("LifecycleMainActivity", "App is paused by the system (Share intent)")
            shareText("App is paused by the system")
        }
    }

    private fun setupNavigationButtons(savedInstanceState: Bundle?) {
        binding.btnSecondActivity.setOnClickListener {
            Log.d("LifecycleMainActivity", "Navigating to SecondActivity")
            savedInstanceState?.getString("SecondActivity")?.also { choose = it }

            Intent(this, SecondActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("info_send", binding.edtSend.text.toString())
            }.also { intent ->
                getResultFromSecondActivity.launch(intent)
            }
        }
    }

    private fun setupLaunchModeButtons() {
        binding.btnStandard.setOnClickListener {
            Log.d("LaunchMode", "Opening StandardActivity")

            val bundle = Bundle().apply {
                putInt("task_id", taskId)
                putString("instance_label", "MainActivity Instance #${instanceCount + 1}")
                putLong("start_time", System.currentTimeMillis())
                putBoolean("is_foreground", true)
            }
            val intent = Intent(this, StandardActivity::class.java).apply {
                putExtras(bundle)
            }

            startActivity(intent)
        }


        binding.btnSingleTop.setOnClickListener {
            Log.d("LaunchMode", "Opening SingleTopActivity")
            launchActivity<SingleTopActivity>()
        }

        binding.btnSingleTask.setOnClickListener {
            Log.d("LaunchMode", "Opening SingleTaskActivity")
            launchActivity<SingleTaskActivity>()
        }

        binding.btnSingleInstance.setOnClickListener {
            Log.d("LaunchMode", "Opening SingleInstanceActivity")
            launchActivity<SingleInstanceActivity>()
        }

        binding.btnIntentFlag.setOnClickListener {
            val launchModes = arrayOf("Standard", "SingleTop", "SingleTask", "SingleInstance")

            AlertDialog.Builder(this)
                .setTitle("Select Launch Mode to demo: ")
                .setItems(launchModes) { _, which ->
                    val intent = Intent(this, IntentFlagActivity::class.java)

                    when (which) {
                        0 -> {
                            //standard khong can gan co
                        }
                        1 -> {
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        2 -> {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        3 -> {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                    }

                    Log.d("LaunchMode", "Opening ${launchModes[which]} | flags=${intent.flags}")
                    intent.putExtra("launch_mode",launchModes[which])
                    startActivity(intent)
                }
                .show()
        }
    }

    private inline fun <reified T : AppCompatActivity> launchActivity() {
        startActivity(Intent(this, T::class.java))
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
            binding.edtSend.setText(text)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("LaunchMode", "onNewIntent triggered for instance: ${System.identityHashCode(this)}")
    }

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
        instanceCount--
        Log.d(
            "LifecycleMainActivity",
            "onDestroy - Instance: ${System.identityHashCode(this)}"
        )
        Log.d("LaunchMode", "Remaining instances: $instanceCount")
    }
}