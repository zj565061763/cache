package com.sd.demo.cache

import android.app.Application
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                .setExceptionHandler {
                    logMsg { "error:$it" }
                }
                .build(filesDir.resolve("app_cache"))
        )
    }
}