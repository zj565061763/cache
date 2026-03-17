package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.cache.databinding.SampleSingleCacheKtxBinding
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.asSingleCacheKtx
import com.sd.lib.cache.update
import kotlinx.coroutines.launch

class SampleSingleCacheKtx : AppCompatActivity() {
  private val _binding by lazy { SampleSingleCacheKtxBinding.inflate(layoutInflater) }
  private val _cache = FCacheKtx.get(DefaultModel::class.java).asSingleCacheKtx()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)

    _binding.btnPut.setOnClickListener {
      lifecycleScope.launch {
        val model = DefaultModel(name = System.currentTimeMillis().toString())
        _cache.edit { put(model) }
      }
    }

    _binding.btnUpdate.setOnClickListener {
      lifecycleScope.launch {
        _cache.update { it.copy(name = "update") }
      }
    }

    _binding.btnRemove.setOnClickListener {
      lifecycleScope.launch {
        _cache.edit { remove() }
      }
    }

    lifecycleScope.launch {
      _cache.flow().collect { data ->
        logMsg { "collect $data" }
      }
    }
  }
}