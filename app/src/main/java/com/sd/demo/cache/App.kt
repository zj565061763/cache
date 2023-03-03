package com.sd.demo.cache;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final CacheConfig cacheConfig = new CacheConfig.Builder()

                // 设置异常监听
                .setExceptionHandler(new Cache.ExceptionHandler() {
                    @Override
                    public void onException(@NonNull Exception e) {
                        Log.e(MainActivity.TAG, String.valueOf(e));
                    }
                })

                // 创建对象
                .build(this);

        // 初始化
        CacheConfig.init(cacheConfig);
    }
}
