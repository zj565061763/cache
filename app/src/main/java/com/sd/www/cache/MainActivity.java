package com.sd.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.disk.FDisk;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY = "key";
    private static final TestModel TEST_MODEL = new TestModel();

    private Cache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        putData();
        getData();
    }

    private Cache getCache()
    {
        if (mCache == null)
        {
            /**
             * 自定义Cache，底层用腾讯微信的MMKV库实现存储
             */
            mCache = new MMKVCache();

            /**
             * 使用内部存储目录"/data/包名/files/disk_file"
             */
            mCache = FDisk.open();

            /**
             * 设置是否支持内存存储
             */
            mCache.setMemorySupport(false);

            /**
             * 设置保存缓存的时候是否加密
             */
            mCache.setEncrypt(false);
        }
        return mCache;
    }

    private void putData()
    {
        getCache().cacheInteger().put(KEY, 1);
        getCache().cacheLong().put(KEY, 22L);
        getCache().cacheFloat().put(KEY, 333.333F);
        getCache().cacheDouble().put(KEY, 4444.4444D);
        getCache().cacheBoolean().put(KEY, true);
        getCache().cacheString().put(KEY, "hello String");
        getCache().cacheObject().put(TEST_MODEL);
    }

    private void getData()
    {
        Log.i(TAG, "getInt:" + getCache().cacheInteger().get(KEY));
        Log.i(TAG, "getLong:" + getCache().cacheLong().get(KEY));
        Log.i(TAG, "getFloat:" + getCache().cacheFloat().get(KEY));
        Log.i(TAG, "getDouble:" + getCache().cacheDouble().get(KEY));
        Log.i(TAG, "getBoolean:" + getCache().cacheBoolean().get(KEY));
        Log.i(TAG, "getString:" + getCache().cacheString().get(KEY));
        Log.i(TAG, "getObject:" + getCache().cacheObject().get(TestModel.class));
    }

    @Override
    public void onClick(View v)
    {
        getData();
    }
}
