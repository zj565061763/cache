package com.sd.www.cache;

import android.app.Application;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheConfig;
import com.sd.lib.cache.disk.FDisk;
import com.sd.www.cache.converter.GlobalEncryptConverter;
import com.sd.www.cache.converter.GsonObjectConverter;

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // 初始化
        FDisk.init(new CacheConfig.Builder()
                // 如果需要加解密，设置全局加解密转换器
                .setEncryptConverter(new GlobalEncryptConverter())
                // 设置全局Gson对象转换器
                .setObjectConverter(new GsonObjectConverter())
                // 设置全局异常监听
                .setExceptionHandler(new Cache.ExceptionHandler()
                {
                    @Override
                    public void onException(Exception e)
                    {

                    }
                })
                .build(this)
        );
    }
}
