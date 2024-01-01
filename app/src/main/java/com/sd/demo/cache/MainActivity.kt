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

    private val _cache = FCache.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)

        _binding.btnPut.setOnClickListener {
            putData()
        }

        _binding.btnGet.setOnClickListener {
            getData()
        }

        _binding.btnRemove.setOnClickListener {
            removeData()
        }
    }


    private fun putData() {
        _cache.cInt().put(Key, 1)
        _cache.cLong().put(Key, 22L)
        _cache.cFloat().put(Key, 333.333f)
        _cache.cDouble().put(Key, 4444.4444)
        _cache.cBoolean().put(Key, true)
        _cache.cString().put(Key, "hello String")

        val model = TestModel()
        _cache.cObject(TestModel::class.java).put(model)
        _cache.cObjects(TestModel::class.java).put(Key, model)
        _cache.cObjects(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInt:" + _cache.cInt().get(Key) }
        logMsg { "cacheLong:" + _cache.cLong().get(Key) }
        logMsg { "cacheFloat:" + _cache.cFloat().get(Key) }
        logMsg { "cacheDouble:" + _cache.cDouble().get(Key) }
        logMsg { "cacheBoolean:" + _cache.cBoolean().get(Key) }
        logMsg { "cacheString:" + _cache.cString().get(Key) }
        logMsg { "objectSingle:" + _cache.cObject(TestModel::class.java).get() }
        logMsg { "objectMulti:" + _cache.cObjects(TestModel::class.java).get(Key) }
        logMsg { "objectMulti:" + _cache.cObjects(TestModel::class.java).get(Key + Key) }

        val keys = _cache.cObjects(TestModel::class.java).keys()
        logMsg { "objectMulti keys:" + keys.joinToString(prefix = "[", postfix = "]", separator = ",") }
    }

    private fun removeData() {
        _cache.cInt().remove(Key)
        _cache.cLong().remove(Key)
        _cache.cFloat().remove(Key)
        _cache.cDouble().remove(Key)
        _cache.cBoolean().remove(Key)
        _cache.cString().remove(Key)
        _cache.cObject(TestModel::class.java).remove()
        _cache.cObjects(TestModel::class.java).remove(Key)
        _cache.cObjects(TestModel::class.java).remove(Key + Key)
    }
}

private fun testPerformance(
    cache: Cache = FCache.get(),
    repeat: Int = 100,
) {
    val content = "1".repeat(1024)
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