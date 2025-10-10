package com.example.androidtutorial

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.databinding.ActivityMainBinding
import com.example.androidtutorial.launchmode.SingleInstanceActivity
import com.example.androidtutorial.launchmode.SingleInstancePerTaskActivity
import com.example.androidtutorial.launchmode.SingleTaskActivity
import com.example.androidtutorial.launchmode.SingleTopActivity
import com.example.androidtutorial.launchmode.StandardActivity

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
            launchActivity<StandardActivity>()
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

        binding.btnSingleInstancePreTask.setOnClickListener {
            Log.d("LaunchMode", "Opening SingleInstancePreTaskActivity")
            launchActivity<SingleInstancePerTaskActivity>()
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