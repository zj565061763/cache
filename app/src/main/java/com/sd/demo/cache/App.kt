package com.sd.demo.cache

import android.app.Application
import com.sd.demo.cache.impl.CacheExceptionHandler
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                .setDirectory(getExternalFilesDir("app_cache")!!)
                .setExceptionHandler(CacheExceptionHandler())
//                .setObjectConverter(MoshiObjectConverter())
//                .setCacheStore(FileCacheStore::class.java)
                .build(this)
        )
    }
}