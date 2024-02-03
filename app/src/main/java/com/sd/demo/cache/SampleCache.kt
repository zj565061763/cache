package com.sd.demo.cache

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.SampleCacheBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache

private const val Key = "key"

class SampleCache : AppCompatActivity() {
    private val _binding by lazy { SampleCacheBinding.inflate(layoutInflater) }

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
        cache.cInt().put(Key, 1).also { logMsg { "cInt:$it" } }
        cache.cLong().put(Key, 22L).also { logMsg { "cLong:$it" } }
        cache.cFloat().put(Key, 333.333f).also { logMsg { "cFloat:$it" } }
        cache.cDouble().put(Key, 4444.4444).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().put(Key, true).also { logMsg { "cBoolean:$it" } }
        cache.cString().put(Key, "hello String").also { logMsg { "cString:$it" } }

        val model = TestModel()
        cache.cObject(TestModel::class.java).put(model).also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).put(Key, model).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).put(Key + Key, model).also { logMsg { "cObjects:$it" } }
    }

    private fun getData(cache: Cache) {
        cache.cInt().get(Key).also { logMsg { "cInt:$it" } }
        cache.cLong().get(Key).also { logMsg { "cLong:$it" } }
        cache.cFloat().get(Key).also { logMsg { "cFloat:$it" } }
        cache.cDouble().get(Key).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().get(Key).also { logMsg { "cBoolean:$it" } }
        cache.cString().get(Key).also { logMsg { "cString:$it" } }
        cache.cObject(TestModel::class.java).get().also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).get(Key).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).get(Key + Key).also { logMsg { "cObjects:$it" } }

        cache.cObjects(TestModel::class.java).keys().also { keys ->
            logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
        }
    }

    private fun containsData(cache: Cache) {
        cache.cInt().contains(Key).also { logMsg { "cInt:$it" } }
        cache.cLong().contains(Key).also { logMsg { "cLong:$it" } }
        cache.cFloat().contains(Key).also { logMsg { "cFloat:$it" } }
        cache.cDouble().contains(Key).also { logMsg { "cDouble:$it" } }
        cache.cBoolean().contains(Key).also { logMsg { "cBoolean:$it" } }
        cache.cString().contains(Key).also { logMsg { "cString:$it" } }
        cache.cObject(TestModel::class.java).contains().also { logMsg { "cObject:$it" } }
        cache.cObjects(TestModel::class.java).contains(Key).also { logMsg { "cObjects:$it" } }
        cache.cObjects(TestModel::class.java).contains(Key + Key).also { logMsg { "cObjects:$it" } }
    }

    private fun removeData(cache: Cache) {
        cache.cInt().remove(Key)
        cache.cLong().remove(Key)
        cache.cFloat().remove(Key)
        cache.cDouble().remove(Key)
        cache.cBoolean().remove(Key)
        cache.cString().remove(Key)
        cache.cObject(TestModel::class.java).remove()
        cache.cObjects(TestModel::class.java).remove(Key)
        cache.cObjects(TestModel::class.java).remove(Key + Key)
        logMsg { "removeData" }
    }
}