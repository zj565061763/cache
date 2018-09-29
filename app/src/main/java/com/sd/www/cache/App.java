package com.sd.www.cache;

import android.app.Application;

import com.sd.lib.cache.Disk;
import com.sd.lib.cache.DiskConfig;
import com.sd.lib.cache.FDisk;
import com.sd.www.cache.converter.GlobalEncryptConverter;
import com.sd.www.cache.converter.GsonObjectConverter;

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // 初始化
        FDisk.init(new DiskConfig.Builder()
                // 如果需要加解密，设置全局加解密转换器
                .encryptConverter(new GlobalEncryptConverter())
                // 设置全局Gson对象转换器
                .objectConverter(new GsonObjectConverter())
                // 设置全局异常监听
                .exceptionHandler(new Disk.ExceptionHandler()
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
