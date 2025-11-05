package com.eco.musicplayer.audioplayer.music.launchmode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eco.musicplayer.audioplayer.music.MainActivity
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.databinding.ActivityStandardBinding
import com.eco.musicplayer.audioplayer.music.models.modelActivity.BundleData
import com.eco.musicplayer.audioplayer.music.viewmodel.MainViewModel
import java.util.Date

class StandardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStandardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStandardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        displayIntentData()

        binding.btnFinish.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        logInstanceInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun displayIntentData() {
        val bundleData = intent.getParcelableExtra<BundleData>("BUNDLE_DATA")

        bundleData?.let {
            binding.layoutInfo.visibility = View.VISIBLE

            binding.txtTaskId.text = getString(R.string.label_task_id, it.taskId)
            binding.txtInstanceLabel.text = getString(R.string.label_instance, it.instanceLabel)

            val dateFormat = java.text.SimpleDateFormat(
                getString(R.string.time_format_pattern),
                java.util.Locale.getDefault()
            )
            binding.txtStartTime.text = getString(
                R.string.label_start_time,
                dateFormat.format(Date(it.startTime))
            )

            val foregroundText = if (it.isForeground) {
                getString(R.string.value_true)
            } else {
                getString(R.string.value_false)
            }
            binding.txtIsForeground.text = getString(R.string.label_is_foreground, foregroundText)
        } ?: run {
            binding.layoutInfo.visibility = View.INVISIBLE
        }
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        setIntent(intent)
//        displayIntentData()
//    }

    private fun logInstanceInfo() {
        MainViewModel.instanceCount++
        val instanceId = System.identityHashCode(this)
        Log.d("LaunchMode", "-----------------------------")
        Log.d("LaunchMode", "Standard Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: ${MainViewModel.instanceCount}")
    }
}