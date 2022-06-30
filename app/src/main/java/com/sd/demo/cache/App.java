package com.sd.demo.cache;

import android.app.Application;

import com.sd.lib.cache.CacheConfig;
import com.sd.lib.cache.store.lru.DiskLruCacheStore;

import java.io.File;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 自定义保存目录
        final File directory = new File(getExternalCacheDir(), "app_disk_cache");

        final CacheConfig cacheConfig = new CacheConfig.Builder()
                // 使用腾讯MMKV自定义的CacheStore，如果不设置，默认使用内部存储目录"/data/包名/files/f_disk_cache"
//                .setCacheStore(new MMKVCacheStore(this))

                // Lru算法MMKV自定义的CacheStore
//                .setCacheStore(new MMKVLruCacheStore(this, 1))

                // 设置限制数量的CacheStore
//                .setCacheStore(DiskLruCacheStore.limitCount(5, directory))

                // 设置限制大小的CacheStore
//                .setCacheStore(DiskLruCacheStore.limitSize(10, directory))

                // 设置对象转换器，如果不设置，使用内部默认的Gson对象转换器
//                .setObjectConverter(new GsonObjectConverter())

                // 设置加解密转换器
//                .setEncryptConverter(new GlobalEncryptConverter())

                // 设置异常监听
//                .setExceptionHandler(new GlobalExceptionHandler())

                // 创建对象
                .build(this);

        // 初始化
        CacheConfig.init(cacheConfig);
    }
}
