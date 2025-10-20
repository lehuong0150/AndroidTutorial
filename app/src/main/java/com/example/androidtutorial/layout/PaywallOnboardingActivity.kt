package com.example.androidtutorial.layout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.databinding.ActivityPaywallOnboardingBinding

class PaywallOnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaywallOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaywallOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}