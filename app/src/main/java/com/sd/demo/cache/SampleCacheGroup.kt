package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleCacheGroupBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache

class SampleCacheGroup : AppCompatActivity() {
    private val key = "key"
    private val _binding by lazy { SampleCacheGroupBinding.inflate(layoutInflater) }

    private val _cache = FCache.currentGroup().unlimited("default")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        logMsg { "currentGroup:${FCache.getCurrentGroup()}" }

        _binding.btnSetGroup.setOnClickListener {
            FCache.setCurrentGroup("666")
            logMsg { "currentGroup:${FCache.getCurrentGroup()}" }
        }
        _binding.btnRemoveGroup.setOnClickListener {
            FCache.setCurrentGroup("")
            logMsg { "currentGroup:${FCache.getCurrentGroup()}" }
        }

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
        cache.cInt().put(key, 1).also { logMsg { "cInt:$it" } }
        cache.cLong().put(key, 22L).also { logMsg { "cLong:$it" } }
        cache.cFloat().put(key, 333.333f).also { logMsg { "cFloat:$it" } }
        cache.cDouble().put(key, 4444.4444).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().put(key, true).also { logMsg { "cBoolean:$it" } }
        cache.cString().put(key, "hello String").also { logMsg { "cString:$it" } }

        val model = TestModel()
        cache.cObject(TestModel::class.java).put(model).also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).put(key, model).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).put(key + key, model).also { logMsg { "cObjects:$it" } }
    }

    private fun getData(cache: Cache) {
        cache.cInt().get(key).also { logMsg { "cInt:$it" } }
        cache.cLong().get(key).also { logMsg { "cLong:$it" } }
        cache.cFloat().get(key).also { logMsg { "cFloat:$it" } }
        cache.cDouble().get(key).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().get(key).also { logMsg { "cBoolean:$it" } }
        cache.cString().get(key).also { logMsg { "cString:$it" } }
        cache.cObject(TestModel::class.java).get().also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).get(key).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).get(key + key).also { logMsg { "cObjects:$it" } }

        cache.cObjects(TestModel::class.java).keys().also { keys ->
            logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
        }
    }

    private fun containsData(cache: Cache) {
        cache.cInt().contains(key).also { logMsg { "cInt:$it" } }
        cache.cLong().contains(key).also { logMsg { "cLong:$it" } }
        cache.cFloat().contains(key).also { logMsg { "cFloat:$it" } }
        cache.cDouble().contains(key).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().contains(key).also { logMsg { "cBoolean:$it" } }
        cache.cString().contains(key).also { logMsg { "cString:$it" } }
        cache.cObject(TestModel::class.java).contains().also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).contains(key).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).contains(key + key).also { logMsg { "cObjects:$it" } }
    }

    private fun removeData(cache: Cache) {
        cache.cInt().remove(key)
        cache.cLong().remove(key)
        cache.cFloat().remove(key)
        cache.cDouble().remove(key)
        cache.cBoolean().remove(key)
        cache.cString().remove(key)
        cache.cObject(TestModel::class.java).remove()
        cache.cObjects(TestModel::class.java).remove(key)
        cache.cObjects(TestModel::class.java).remove(key + key)
        logMsg { "removeData" }
    }

    override fun onDestroy() {
        super.onDestroy()
        FCache.setCurrentGroup("")
        logMsg { "currentGroup:${FCache.getCurrentGroup()}" }
    }
}