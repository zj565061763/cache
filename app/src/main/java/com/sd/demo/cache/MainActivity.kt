package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.FCache
import kotlin.time.measureTime

private const val Key = "key"

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val _defaultGroupCache = FCache.get()
    private val _currentGroupCache = FCache.currentGroup().unlimited("unlimited")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        FCache.setCurrentGroup("1")

        _binding.btnPut.setOnClickListener {
            putData(_defaultGroupCache)
            putData(_currentGroupCache)
        }

        _binding.btnGet.setOnClickListener {
            getData(_defaultGroupCache)
            getData(_currentGroupCache)
        }

        _binding.btnRemove.setOnClickListener {
            removeData(_defaultGroupCache)
            removeData(_currentGroupCache)
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

private fun testPerformance(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    val content = "1".repeat(10 * 1024)
    measureTime {
        repeat(repeat) { index ->
            cache.cString().put(index.toString(), content)
        }
    }.let {
        logMsg { "time:${it.inWholeMilliseconds}" }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}