package com.example.androidtutorial

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    lateinit var btnBack :Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener{
            this.finish()
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("LifecycleSecondActivity", "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("LifecycleSecondActivity", "onReStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("LifecycleSecondActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("LifecycleSecondActivity", "onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LifecycleSecondActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LifecycleSecondActivity", "onDestroy")
    }
}