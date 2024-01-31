package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
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
        _binding.btnRemove.setOnClickListener {
            removeData(_cache)
        }
    }


    private fun putData(cache: Cache) {
        cache.cInt().put(Key, 1)
        cache.cLong().put(Key, 22L)
        cache.cFloat().put(Key, 333.333f)
        cache.cDouble().put(Key, 4444.4444)
        cache.cBoolean().put(Key, true)
        cache.cString().put(Key, "hello String")

        val model = TestModel()
        cache.cObject(TestModel::class.java).put(model)
        cache.cObjects(TestModel::class.java).put(Key, model)
        cache.cObjects(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData(cache: Cache) {
        logMsg { "cacheInt:" + cache.cInt().get(Key) }
        logMsg { "cacheLong:" + cache.cLong().get(Key) }
        logMsg { "cacheFloat:" + cache.cFloat().get(Key) }
        logMsg { "cacheDouble:" + cache.cDouble().get(Key) }
        logMsg { "cacheBoolean:" + cache.cBoolean().get(Key) }
        logMsg { "cacheString:" + cache.cString().get(Key) }
        logMsg { "objectSingle:" + cache.cObject(TestModel::class.java).get() }
        logMsg { "objectMulti:" + cache.cObjects(TestModel::class.java).get(Key) }
        logMsg { "objectMulti:" + cache.cObjects(TestModel::class.java).get(Key + Key) }

        val keys = cache.cObjects(TestModel::class.java).keys()
        logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
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
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}