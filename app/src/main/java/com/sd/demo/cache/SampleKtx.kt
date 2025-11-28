package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.cache.databinding.SampleKtxBinding
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.put
import com.sd.lib.cache.remove
import kotlinx.coroutines.launch

class SampleKtx : AppCompatActivity() {
  private val _binding by lazy { SampleKtxBinding.inflate(layoutInflater) }

  private val key = "key"
  private val _cache = FCacheKtx.get(DefaultModel::class.java)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)

    _binding.btnPut.setOnClickListener {
      lifecycleScope.launch {
        _cache.put(key, DefaultModel(name = System.currentTimeMillis().toString()))
      }
    }

    _binding.btnRemove.setOnClickListener {
      lifecycleScope.launch {
        _cache.remove(key)
      }
    }

    lifecycleScope.launch {
      _cache.flowOf(key).collect { data ->
        logMsg { "collect:$data" }
      }
    }
  }
}