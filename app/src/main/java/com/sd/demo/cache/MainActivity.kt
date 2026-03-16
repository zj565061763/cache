package com.sd.demo.cache

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)
    _binding.btnSample.setOnClickListener {
      startActivity(Intent(this, Sample::class.java))
    }
    _binding.btnSampleOtherProcess.setOnClickListener {
      startActivity(Intent(this, SampleOtherProcess::class.java))
    }
    _binding.btnSampleKtx.setOnClickListener {
      startActivity(Intent(this, SampleKtx::class.java))
    }
    _binding.btnSamplePerformance.setOnClickListener {
      startActivity(Intent(this, SamplePerformance::class.java))
    }
  }
}

inline fun logMsg(block: () -> String) {
  Log.i("sd-demo", block())
}