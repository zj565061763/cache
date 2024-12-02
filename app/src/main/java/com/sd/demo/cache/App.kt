package com.sd.demo.cache

import android.app.Application
import com.sd.demo.cache.impl.AppCacheExceptionHandler
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.init

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(this) {
            // 设置异常处理
            setExceptionHandler(AppCacheExceptionHandler())
        }
    }
}