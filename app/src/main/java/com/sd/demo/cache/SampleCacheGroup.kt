package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleCacheGroupBinding
import com.sd.lib.cache.FCache

class SampleCacheGroup : AppCompatActivity() {
    private val _binding by lazy { SampleCacheGroupBinding.inflate(layoutInflater) }

    private val key = "key"

    private val _singleCache = FCache.getActive().single(TestModel::class.java)
    private val _multiCache = FCache.getActive().multi(TestModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }

        _binding.btnSetGroup.setOnClickListener {
            FCache.setActiveGroup("Tom")
            logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
        }
        _binding.btnRemoveGroup.setOnClickListener {
            FCache.setActiveGroup("")
            logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
        }

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
        val model = TestModel()
        _singleCache.put(model).also { logMsg { "single:$it" } }

        _multiCache.put(key, model).also { logMsg { "multi:$it" } }
        _multiCache.put(key + key, model).also { logMsg { "multi:$it" } }
    }

    private fun getData() {
        _singleCache.get().also { logMsg { "single:$it" } }

        _multiCache.get(key).also { logMsg { "multi:$it" } }
        _multiCache.get(key + key).also { logMsg { "multi:$it" } }
        _multiCache.keys().also { keys ->
            logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
        }
    }

    private fun containsData() {
        _singleCache.contains().also { logMsg { "single:$it" } }

        _multiCache.contains(key).also { logMsg { "multi:$it" } }
        _multiCache.contains(key + key).also { logMsg { "multi:$it" } }
    }

    private fun removeData() {
        _singleCache.remove()

        _multiCache.remove(key)
        _multiCache.remove(key + key)
        logMsg { "removeData" }
    }

    override fun onDestroy() {
        super.onDestroy()
        FCache.setActiveGroup("")
        logMsg { "getActiveGroup:${FCache.getActiveGroup()}" }
    }
}