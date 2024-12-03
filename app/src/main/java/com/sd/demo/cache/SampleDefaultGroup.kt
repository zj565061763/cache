package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleDefaultGroupBinding
import com.sd.lib.cache.FCache

class SampleDefaultGroup : AppCompatActivity() {
    private val _binding by lazy { SampleDefaultGroupBinding.inflate(layoutInflater) }

    private val key = "key"
    private val _cache = FCache.get(DefaultModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnPut.setOnClickListener {
            putData()
        }
        _binding.btnGet.setOnClickListener {
            getData()
        }
        _binding.btnContains.setOnClickListener {
            containsData()
        }
        _binding.btnRemove.setOnClickListener {
            removeData()
        }
    }

    private fun putData() {
        val model = DefaultModel()
        _cache.put(key, model).also { logMsg { "put1:$it" } }
        _cache.put(key + key, model).also { logMsg { "put2:$it" } }
    }

    private fun getData() {
        _cache.get(key).also { logMsg { "get1:$it" } }
        _cache.get(key + key).also { logMsg { "get2:$it" } }
        _cache.keys().also { keys ->
            logMsg { "keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
        }
    }

    private fun containsData() {
        _cache.contains(key).also { logMsg { "contains1:$it" } }
        _cache.contains(key + key).also { logMsg { "contains2:$it" } }
    }

    private fun removeData() {
        _cache.remove(key)
        _cache.remove(key + key)
        logMsg { "removeData" }
    }
}