package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sd.demo.cache.databinding.SampleSingleCacheKtxBinding
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.asSingleCacheKtx
import kotlinx.coroutines.launch

class SampleSingleCacheKtx : AppCompatActivity() {
  private val _binding by lazy { SampleSingleCacheKtxBinding.inflate(layoutInflater) }
  private val _cache = FCacheKtx.get(DefaultModel::class.java).asSingleCacheKtx { DefaultModel() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)

    _binding.btnUpdate.setOnClickListener {
      lifecycleScope.launch {
        _cache.update { it.copy(name = "update") }
      }
    }

    _binding.btnUpdateWithTime.setOnClickListener {
      lifecycleScope.launch {
        _cache.update { it.copy(name = "update:${System.currentTimeMillis()}") }
      }
    }

    _binding.btnRemove.setOnClickListener {
      lifecycleScope.launch {
        // 返回null，会删除缓存，还原为默认缓存
        _cache.update { null }
      }
    }

    lifecycleScope.launch {
      _cache.flow().collect { data ->
        logMsg { "collect $data" }
      }
    }
  }
}