package com.sd.www.cache;

import android.app.Application;

import com.sd.lib.cache.CacheConfig;
import com.sd.lib.cache.FCache;
import com.sd.www.cache.converter.GlobalEncryptConverter;
import com.sd.www.cache.converter.GlobalExceptionHandler;
import com.sd.www.cache.converter.GsonObjectConverter;

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // 初始化
        FCache.init(new CacheConfig.Builder()
                /**
                 * 使用腾讯MMKV自定义的CacheStore，如果不设置，默认使用内部存储目录"/data/包名/files/disk_file"
                 */
                .setDiskCacheStore(new MMKVCacheStore(this))
                /**
                 * 设置对象转换器
                 */
                .setObjectConverter(new GsonObjectConverter())
                /**
                 * 设置加解密转换器
                 */
                .setEncryptConverter(new GlobalEncryptConverter())
                /**
                 * 设置异常监听
                 */
                .setExceptionHandler(new GlobalExceptionHandler())
                .build(this)
        );
    }
}
