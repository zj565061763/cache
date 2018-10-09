package com.sd.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.FCache;

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
             * 使用本地磁盘缓存
             * <p>
             * 默认使用内部存储目录"/data/包名/files/disk_file"，可以在初始化的时候设置{@link CacheConfig.Builder#setDiskCacheStore(CacheStore)}
             */
            mCache = FCache.disk();

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
        SDTimeLogger.test(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < 100; i++)
                {
                    getCache().cacheString().put(KEY, "hello String");
                    getCache().cacheString().get(KEY);
                }
            }
        });
    }
}
