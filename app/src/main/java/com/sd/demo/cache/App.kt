package com.sd.demo.cache

import android.app.Application
import com.sd.demo.cache.impl.CacheExceptionHandler
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                // 监听异常信息
                .setExceptionHandler(CacheExceptionHandler())
                .build(this)
        )
    }
}