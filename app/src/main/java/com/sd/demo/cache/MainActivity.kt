package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.cache.databinding.ActivityMainBinding
import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.FCache

private const val Key = "key"

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val cache: Cache by lazy {
        /**
         * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置[CacheConfig.Builder.setCacheStore]
         */
        FCache.disk()
    }

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
        cache.cacheInt().put(Key, 1)
        cache.cacheLong().put(Key, 22L)
        cache.cacheFloat().put(Key, 333.333f)
        cache.cacheDouble().put(Key, 4444.4444)
        cache.cacheBoolean().put(Key, true)
        cache.cacheString().put(Key, "hello String")

        val model = TestModel()
        cache.cacheObject().put(model)
        cache.objectMulti(TestModel::class.java).put(Key, model)
        cache.objectMulti(TestModel::class.java).put(Key + Key, model)
    }

    private fun getData() {
        logMsg { "cacheInt:" + cache.cacheInt().get(Key) }
        logMsg { "cacheLong:" + cache.cacheLong().get(Key) }
        logMsg { "cacheFloat:" + cache.cacheFloat().get(Key) }
        logMsg { "cacheDouble:" + cache.cacheDouble().get(Key) }
        logMsg { "cacheBoolean:" + cache.cacheBoolean().get(Key) }
        logMsg { "cacheString:" + cache.cacheString().get(Key) }
        logMsg { "cacheObject:" + cache.cacheObject().get(TestModel::class.java) }
        logMsg { "objectMulti:" + cache.objectMulti(TestModel::class.java).get(Key) }
        logMsg { "objectMulti:" + cache.objectMulti(TestModel::class.java).get(Key + Key) }
    }

    private fun removeData() {
        cache.cacheInt().remove(Key)
        cache.cacheLong().remove(Key)
        cache.cacheFloat().remove(Key)
        cache.cacheDouble().remove(Key)
        cache.cacheBoolean().remove(Key)
        cache.cacheString().remove(Key)
        cache.cacheObject().remove(TestModel::class.java)
        cache.objectMulti(TestModel::class.java).remove(Key)
        cache.objectMulti(TestModel::class.java).remove(Key + Key)
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}