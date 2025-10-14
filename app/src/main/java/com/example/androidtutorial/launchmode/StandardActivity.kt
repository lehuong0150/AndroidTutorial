package com.example.androidtutorial.launchmode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.MainActivity
import com.example.androidtutorial.MainActivity.Companion.instanceCount
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityStandardBinding
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
        //button quay lai MainActivity ma khong destroy() StandardActivity
        binding.btnFinish.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        logInstanceInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun displayIntentData() {
        val extras = intent.extras
        extras?.let {
            binding.layoutInfo.visibility = View.VISIBLE
            val taskId = extras.getInt("task_id", -1)
            val instanceLabel = extras.getString("instance_label") ?: "N/A"
            val startTime = extras.getLong("start_time", 0L)
            val isForeground = extras.getBoolean("is_foreground", false)

            binding.txtTaskId.text = "Task ID: $taskId"
            binding.txtInstanceLabel.text = "Instance: $instanceLabel"
            binding.txtStartTime.text = "Start Time: ${Date(startTime)}"
            binding.txtIsForeground.text = "Is Foreground: $isForeground"
        } ?: run {
            binding.layoutInfo.visibility = View.INVISIBLE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(
            "LaunchMode", "onNewIntent triggered for instance: " +
                    "${System.identityHashCode(this)}"
        )
    }

    private fun logInstanceInfo() {
        instanceCount++
        val instanceId = System.identityHashCode(this)
        Log.d("LaunchMode", "-----------------------------")
        Log.d("LaunchMode", "Standard Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: $instanceCount")
    }
}