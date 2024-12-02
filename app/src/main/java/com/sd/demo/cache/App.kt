package com.sd.demo.cache

import android.app.Application
import com.sd.demo.cache.impl.AppCacheExceptionHandler
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                // 设置缓存目录
                .setDirectory(getExternalFilesDir("app_cache")!!)
                // 设置异常处理
                .setExceptionHandler(AppCacheExceptionHandler())
                .build(this)
        )
    }
}