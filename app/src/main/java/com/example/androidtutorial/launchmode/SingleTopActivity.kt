package com.example.androidtutorial.launchmode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidtutorial.MainActivity.Companion.instanceCount
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivitySingleTopBinding

class SingleTopActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_top)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnClick.setOnClickListener {
            val intent = Intent(this, SingleTopActivity::class.java)
            startActivity(intent)
        }
        logInstanceInfo()
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
        Log.d("LaunchMode", "SingleTop Activity created")
        Log.d("LaunchMode", "Task ID: $taskId")
        Log.d("LaunchMode", "Instance ID: $instanceId")
        Log.d("LaunchMode", "Total instances: $instanceCount")
    }
}