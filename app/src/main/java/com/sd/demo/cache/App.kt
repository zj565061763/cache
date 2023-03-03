package com.sd.demo.cache

import android.app.Application
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = CacheConfig.Builder()
            .setExceptionHandler { logMsg { "error:$it" } }
            .build(this)

        CacheConfig.init(config)
    }
}