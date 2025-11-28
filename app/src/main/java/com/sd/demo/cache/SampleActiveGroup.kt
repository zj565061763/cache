package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleActiveGroupBinding
import com.sd.lib.cache.FCache

class SampleActiveGroup : AppCompatActivity() {
  private val _binding by lazy { SampleActiveGroupBinding.inflate(layoutInflater) }

  private val _cache = FCache.get(ActiveModel::class.java)

  private val _key1 = "key1"
  private val _key2 = "key2"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(_binding.root)
    logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }

    _binding.btnSetGroup.setOnClickListener {
      FCache.setActiveGroup("1000")
      logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
    }
    _binding.btnRemoveGroup.setOnClickListener {
      FCache.setActiveGroup("")
      logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
    }

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
    val model = ActiveModel()
    _cache.put(_key1, model).also { logMsg { "put key1:$it" } }
    _cache.put(_key2, model).also { logMsg { "put key2:$it" } }
  }

  private fun getCache() {
    _cache.get(_key1).also { logMsg { "get key1:$it" } }
    _cache.get(_key2).also { logMsg { "get key2:$it" } }
    _cache.keys().also { keys ->
      logMsg { "keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
    }
  }

  private fun containsCache() {
    _cache.contains(_key1).also { logMsg { "contains key1:$it" } }
    _cache.contains(_key2).also { logMsg { "contains key2:$it" } }
  }

  private fun removeCache() {
    logMsg { "removeCache" }
    _cache.remove(_key1)
    _cache.remove(_key2)
  }

  override fun onDestroy() {
    super.onDestroy()
    FCache.setActiveGroup("")
    logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
  }
}