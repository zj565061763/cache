package com.sd.demo.cache

import android.app.Application
import com.sd.demo.cache.impl.CacheExceptionHandler
import com.sd.demo.cache.impl.MoshiObjectConverter
import com.sd.lib.cache.CacheConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CacheConfig.init(
            CacheConfig.Builder()
                // 设置缓存目录
                .setDirectory(getExternalFilesDir("app_cache")!!)
                // 设置异常处理
                .setExceptionHandler(CacheExceptionHandler())
                // 设置对象转换器，默认为Gson转换器
                .setObjectConverter(MoshiObjectConverter())
                .build(this)
        )
    }
}