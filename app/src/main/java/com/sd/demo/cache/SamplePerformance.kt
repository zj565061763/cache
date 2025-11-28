package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SamplePerformanceBinding
import com.sd.lib.cache.FCache
import kotlin.time.measureTime

class SamplePerformance : AppCompatActivity() {
  private val _binding by lazy { SamplePerformanceBinding.inflate(layoutInflater) }

  private val _cache = FCache.get(DefaultModel::class.java)

  private val _key = "performance"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)
    _binding.btnPut.setOnClickListener {
      testPut()
    }
    _binding.btnGet.setOnClickListener {
      testGet()
    }
  }

  private fun testPut(repeat: Int = 100) {
    val model = DefaultModel()
    measureTime {
      repeat(repeat) {
        _cache.put(_key, model)
      }
    }.also {
      logMsg { "put time:${it.inWholeMilliseconds}" }
    }
  }

  private fun testGet(repeat: Int = 100) {
    val key = "key"
    measureTime {
      repeat(repeat) {
        _cache.get(key)
      }
    }.also {
      logMsg { "get time:${it.inWholeMilliseconds}" }
    }
  }
}