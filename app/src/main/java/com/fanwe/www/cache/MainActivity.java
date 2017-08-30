package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fanwe.library.cache.SDDiskCache;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private String key = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SDDiskCache.init(this); //初始化
        SDDiskCache.setGlobalObjectConverter(new GlobalObjectConverter());//如果要用XXXObject方法，需要配置Object对象转换器
        SDDiskCache.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，需要配置加解密转换器

        SDDiskCache.open().putInt(key, 1);
        SDDiskCache.open().putLong(key, 2);
        SDDiskCache.open().putFloat(key, 3.3f);
        SDDiskCache.open().putDouble(key, 4.4444d);
        SDDiskCache.open().putBoolean(key, true);
        SDDiskCache.open().putString(key, "hello String", true); //加密
        SDDiskCache.open().putSerializable(new TestModel());
        SDDiskCache.open().putObject(new TestModel(), true); //加密实体

        print();
    }

    private void print()
    {
        Log.i(TAG, "getInt:" + SDDiskCache.open().getInt(key, 0));
        Log.i(TAG, "getLong:" + SDDiskCache.open().getLong(key, 0));
        Log.i(TAG, "getFloat:" + SDDiskCache.open().getFloat(key, 0));
        Log.i(TAG, "getDouble:" + SDDiskCache.open().getDouble(key, 0));
        Log.i(TAG, "getBoolean:" + SDDiskCache.open().getBoolean(key, false));
        Log.i(TAG, "getString:" + SDDiskCache.open().getString(key));
        Log.i(TAG, "getSerializable:" + SDDiskCache.open().getSerializable(TestModel.class));
        Log.i(TAG, "getObject:" + SDDiskCache.open().getObject(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete(); //删除该目录对应的所有缓存
    }
}
