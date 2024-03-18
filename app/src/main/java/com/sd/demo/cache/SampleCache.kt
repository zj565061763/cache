package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleCacheBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache

class SampleCache : AppCompatActivity() {
    private val _binding by lazy { SampleCacheBinding.inflate(layoutInflater) }

    private val key = "key"
    private val _cache = FCache.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnPut.setOnClickListener {
            putData(_cache)
        }
        _binding.btnGet.setOnClickListener {
            getData(_cache)
        }
        _binding.btnContains.setOnClickListener {
            containsData(_cache)
        }
        _binding.btnRemove.setOnClickListener {
            removeData(_cache)
        }
    }

    private fun putData(cache: Cache) {
        val model = TestModel()
        cache.o(TestModel::class.java).put(model).also { logMsg { "cObject:$it" } }
        cache.oo(TestModel::class.java).put(key, model).also { logMsg { "cObjects:$it" } }
        cache.oo(TestModel::class.java).put(key + key, model).also { logMsg { "cObjects:$it" } }
    }

    private fun getData(cache: Cache) {
        cache.o(TestModel::class.java).get().also { logMsg { "cObject:$it" } }
        cache.oo(TestModel::class.java).get(key).also { logMsg { "cObjects:$it" } }
        cache.oo(TestModel::class.java).get(key + key).also { logMsg { "cObjects:$it" } }

        cache.oo(TestModel::class.java).keys().also { keys ->
            logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
        }
    }

    private fun containsData(cache: Cache) {
        cache.o(TestModel::class.java).contains().also { logMsg { "cObject:$it" } }
        cache.oo(TestModel::class.java).contains(key).also { logMsg { "cObjects:$it" } }
        cache.oo(TestModel::class.java).contains(key + key).also { logMsg { "cObjects:$it" } }
    }

    private fun removeData(cache: Cache) {
        cache.o(TestModel::class.java).remove()
        cache.oo(TestModel::class.java).remove(key)
        cache.oo(TestModel::class.java).remove(key + key)
        logMsg { "removeData" }
    }
}