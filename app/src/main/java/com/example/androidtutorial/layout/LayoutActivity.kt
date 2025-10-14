package com.example.androidtutorial.layout

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityLayoutBinding

class LayoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val newestAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.newest_filter_options,
            android.R.layout.simple_spinner_item

        )
        newestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNewest.adapter = newestAdapter

        val priceAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.price_filter_options,
            android.R.layout.simple_spinner_item

        )
        newestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPrice.adapter = priceAdapter
    }
}