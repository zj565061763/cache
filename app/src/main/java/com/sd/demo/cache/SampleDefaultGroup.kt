package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleDefaultGroupBinding
import com.sd.lib.cache.FCache

open class SampleDefaultGroup : AppCompatActivity() {
  private val _binding by lazy { SampleDefaultGroupBinding.inflate(layoutInflater) }

  private val _cache = FCache.get(DefaultModel::class.java)

  private val key1 = "key1"
  private val key2 = "key2"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)
    _binding.btnPut.setOnClickListener {
      putCache()
    }
    _binding.btnGet.setOnClickListener {
      getCache()
    }
    _binding.btnContains.setOnClickListener {
      containsCache()
    }
    _binding.btnRemove.setOnClickListener {
      removeCache()
    }
  }

  private fun putCache() {
    val model = DefaultModel()
    _cache.put(key1, model).also { logMsg { "put key1:$it" } }
    _cache.put(key2, model).also { logMsg { "put key2:$it" } }
  }

  private fun getCache() {
    _cache.get(key1).also { logMsg { "get key1:$it" } }
    _cache.get(key2).also { logMsg { "get key2:$it" } }
    _cache.keys().also { keys ->
      logMsg { "keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
    }
  }

  private fun containsCache() {
    _cache.contains(key1).also { logMsg { "contains key1:$it" } }
    _cache.contains(key2).also { logMsg { "contains key2:$it" } }
  }

  private fun removeCache() {
    logMsg { "removeCache" }
    _cache.remove(key1)
    _cache.remove(key2)
  }
}

class SampleDefaultGroupOtherProcess : SampleDefaultGroup()