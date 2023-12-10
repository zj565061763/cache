package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding
import com.sd.lib.cache.FCache

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
        _cache.objectSingle(TestModel::class.java).put(model)
        _cache.objectMulti(TestModel::class.java).put(Key, model)
        _cache.objectMulti(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInt:" + _cache.cInt().get(Key) }
        logMsg { "cacheLong:" + _cache.cLong().get(Key) }
        logMsg { "cacheFloat:" + _cache.cFloat().get(Key) }
        logMsg { "cacheDouble:" + _cache.cDouble().get(Key) }
        logMsg { "cacheBoolean:" + _cache.cDouble().get(Key) }
        logMsg { "cacheString:" + _cache.cString().get(Key) }
        logMsg { "objectSingle:" + _cache.objectSingle(TestModel::class.java).get() }
        logMsg { "objectMulti:" + _cache.objectMulti(TestModel::class.java).get(Key) }
        logMsg { "objectMulti:" + _cache.objectMulti(TestModel::class.java).get(Key + Key) }
    }

    private fun removeData() {
        _cache.cInt().remove(Key)
        _cache.cLong().remove(Key)
        _cache.cFloat().remove(Key)
        _cache.cDouble().remove(Key)
        _cache.cDouble().remove(Key)
        _cache.cString().remove(Key)
        _cache.objectSingle(TestModel::class.java).remove()
        _cache.objectMulti(TestModel::class.java).remove(Key)
        _cache.objectMulti(TestModel::class.java).remove(Key + Key)
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}