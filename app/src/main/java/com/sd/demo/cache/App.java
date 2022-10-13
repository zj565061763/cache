package com.sd.demo.cache;

import android.app.Application;

import com.sd.lib.cache.CacheConfig;
import com.sd.lib.cache.store.LimitedSizeDiskLruCacheStore;

import java.io.File;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final CacheConfig cacheConfig = new CacheConfig.Builder()

                // 使用腾讯MMKV自定义的CacheStore，如果不设置，默认使用内部存储目录"/data/包名/files/f_disk_cache"
//                .setCacheStore(new MMKVCacheStore(this))

                // 设置限制大小的CacheStore
//                .setCacheStore(new LimitedSizeDiskLruCacheStore(new File(getExternalCacheDir(), "app_disk_cache"), 10))

                // 创建对象
                .build(this);

        // 初始化
        CacheConfig.init(cacheConfig);
    }
}
