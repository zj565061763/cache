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

  private val key1 = "key1"
  private val key2 = "key2"

  private val _cache = FCacheKtx.get(DefaultModel::class.java)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)

    _binding.btnPut.setOnClickListener {
      lifecycleScope.launch {
        val model = DefaultModel(name = System.currentTimeMillis().toString())
        _cache.put(key1, model)
        _cache.put(key2, model)
      }
    }

    _binding.btnRemove.setOnClickListener {
      lifecycleScope.launch {
        _cache.remove(key1)
        _cache.remove(key2)
      }
    }

    lifecycleScope.launch {
      _cache.flowOf(key1).collect { data ->
        logMsg { "collect key1 $data" }
      }
    }

    lifecycleScope.launch {
      _cache.flowOf(key2).collect { data ->
        logMsg { "collect key2 $data" }
      }
    }

    lifecycleScope.launch {
      _cache.flowOfKeys().collect { keys ->
        logMsg { "collect keys $keys" }
      }
    }
  }
}