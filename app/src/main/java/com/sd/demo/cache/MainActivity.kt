package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.fCache

private const val Key = "key"

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val _cache: Cache = fCache

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
        _cache.cacheInt().put(Key, 1)
        _cache.cacheLong().put(Key, 22L)
        _cache.cacheFloat().put(Key, 333.333f)
        _cache.cacheDouble().put(Key, 4444.4444)
        _cache.cacheBoolean().put(Key, true)
        _cache.cacheString().put(Key, "hello String")

        val model = TestModel()
        _cache.cacheObject().put(model)
        _cache.objectMulti(TestModel::class.java).put(Key, model)
        _cache.objectMulti(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInt:" + _cache.cacheInt().get(Key) }
        logMsg { "cacheLong:" + _cache.cacheLong().get(Key) }
        logMsg { "cacheFloat:" + _cache.cacheFloat().get(Key) }
        logMsg { "cacheDouble:" + _cache.cacheDouble().get(Key) }
        logMsg { "cacheBoolean:" + _cache.cacheBoolean().get(Key) }
        logMsg { "cacheString:" + _cache.cacheString().get(Key) }
        logMsg { "cacheObject:" + _cache.cacheObject().get(TestModel::class.java) }
        logMsg { "objectMulti:" + _cache.objectMulti(TestModel::class.java).get(Key) }
        logMsg { "objectMulti:" + _cache.objectMulti(TestModel::class.java).get(Key + Key) }
    }

    private fun removeData() {
        _cache.cacheInt().remove(Key)
        _cache.cacheLong().remove(Key)
        _cache.cacheFloat().remove(Key)
        _cache.cacheDouble().remove(Key)
        _cache.cacheBoolean().remove(Key)
        _cache.cacheString().remove(Key)
        _cache.cacheObject().remove(TestModel::class.java)
        _cache.objectMulti(TestModel::class.java).remove(Key)
        _cache.objectMulti(TestModel::class.java).remove(Key + Key)
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}