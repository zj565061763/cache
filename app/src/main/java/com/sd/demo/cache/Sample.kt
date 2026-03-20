package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleBinding
import com.sd.lib.cache.FCache

open class Sample : AppCompatActivity() {
  private val _binding by lazy { SampleBinding.inflate(layoutInflater) }

  private val _cache = FCache.get(DefaultModel::class.java)

  private val _key1 = "key1"
  private val _key2 = "key2"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)
    _binding.btnPut.setOnClickListener {
      putCache()
    }
    _binding.btnGet.setOnClickListener {
      getCache()
    }
    _binding.btnRemove.setOnClickListener {
      removeCache()
    }
  }

  private fun putCache() {
    val model = DefaultModel(name = System.currentTimeMillis().toString())
    _cache.put(_key1, model).also { logMsg { "put key1:$it" } }
    _cache.put(_key2, model).also { logMsg { "put key2:$it" } }
  }

  private fun getCache() {
    _cache.get(_key1).also { logMsg { "get key1:$it" } }
    _cache.get(_key2).also { logMsg { "get key2:$it" } }
  }

  private fun removeCache() {
    logMsg { "removeCache" }
    _cache.remove(_key1)
    _cache.remove(_key2)
  }
}

class SampleOtherProcess : Sample()