package com.example.androidtutorial

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    lateinit var txtText: TextView
    lateinit var btnFinish: Button
    lateinit var btnRotation: Button
    lateinit var btnSecondActivity: Button
    var choose: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LifecycleActivity", "onCreate")

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtText = findViewById(R.id.txtChoose)
        btnFinish = findViewById(R.id.btnFinish)
        btnRotation = findViewById(R.id.btnRotation)
        btnSecondActivity = findViewById(R.id.btnSecondActivity)
        choose = savedInstanceState?.getString("0")

        btnFinish.setOnClickListener {
            choose = savedInstanceState?.getString("Finish")
            this.finish()
        }
        btnRotation.setOnClickListener {
            choose = savedInstanceState?.getString("Rotation")
        }
        btnSecondActivity.setOnClickListener{
            choose = savedInstanceState?.getString("SecondActivity")
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        txtText.text = savedInstanceState?.getString(choose)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState?.run {
            putString(choose, txtText.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        Log.d("LifecycleMainActivity", "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LifecycleMainActivity", "onReStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LifecycleMainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifecycleMainActivity", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifecycleMainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifecycleMainActivity", "onDestroy")
    }
}