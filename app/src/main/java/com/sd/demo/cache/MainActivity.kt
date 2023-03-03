package com.sd.demo.cache

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sd.lib.cache.Cache
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.FCache

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val key = "key"
    private val model = TestModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val cache: Cache by lazy {
        /**
         * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置[CacheConfig.Builder.setCacheStore]
         */
        FCache.disk()
    }

    private fun putData() {
        cache.cacheInteger().put(key, 1)
        cache.cacheLong().put(key, 22L)
        cache.cacheFloat().put(key, 333.333f)
        cache.cacheDouble().put(key, 4444.4444)
        cache.cacheBoolean().put(key, true)
        cache.cacheString().put(key, "hello String")
        cache.cacheObject().put(model)
        cache.cacheMultiObject(TestModel::class.java).put(key, model)
        cache.cacheMultiObject(TestModel::class.java).put(key + key, model)
    }

    private fun getData() {
        logMsg { "cacheInteger:" + cache.cacheInteger().get(key) }
        logMsg { "cacheLong:" + cache.cacheLong().get(key) }
        logMsg { "cacheFloat:" + cache.cacheFloat().get(key) }
        logMsg { "cacheDouble:" + cache.cacheDouble().get(key) }
        logMsg { "cacheBoolean:" + cache.cacheBoolean().get(key) }
        logMsg { "cacheString:" + cache.cacheString().get(key) }
        logMsg { "cacheObject:" + cache.cacheObject().get(TestModel::class.java) }
        logMsg { "cacheMultiObject:" + cache.cacheMultiObject(TestModel::class.java).get(key) }
        logMsg { "cacheMultiObject:" + cache.cacheMultiObject(TestModel::class.java).get(key + key) }
    }

    private fun removeData() {
        cache.cacheInteger().remove(key)
        cache.cacheLong().remove(key)
        cache.cacheFloat().remove(key)
        cache.cacheDouble().remove(key)
        cache.cacheBoolean().remove(key)
        cache.cacheString().remove(key)
        cache.cacheObject().remove(TestModel::class.java)
        cache.cacheMultiObject(TestModel::class.java).remove(key)
        cache.cacheMultiObject(TestModel::class.java).remove(key + key)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_put -> putData()
            R.id.btn_get -> getData()
            R.id.btn_remove -> removeData()
            else -> {}
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("cache-demo", block())
}