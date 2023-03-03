package com.sd.demo.cache

import android.app.Application
import android.util.Log
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                .setExceptionHandler { Log.e(MainActivity.TAG, it.toString()) }
                .build(this)
        )
    }
}