package com.sd.demo.cache

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnSampleCache.setOnClickListener {
            startActivity(Intent(this, SampleCache::class.java))
        }

        _binding.btnSamplePerformance.setOnClickListener {
            startActivity(Intent(this, SamplePerformance::class.java))
        }
    }
}