package com.sd.www.cache;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.FCache;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY = "key";
    private static final TestModel TEST_MODEL = new TestModel();

    private Cache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        putData();
        getData();
    }

    private Cache getCache() {
        if (mCache == null) {
            /**
             * 使用本地磁盘缓存，
             * 默认使用内部存储目录"/data/包名/files/disk_file"，可以在初始化的时候设置{@link CacheConfig.Builder#setDiskCacheStore(CacheStore)}
             */
            mCache = FCache.disk();

            // 设置是否支持内存存储，默认false
            mCache.setMemorySupport(false);

            // 设置保存缓存的时候是否加密，默认false
            mCache.setEncrypt(false);
        }
        return mCache;
    }

    private void putData() {
        getCache().cacheInteger().put(KEY, 1);
        getCache().cacheLong().put(KEY, 22L);
        getCache().cacheFloat().put(KEY, 333.333F);
        getCache().cacheDouble().put(KEY, 4444.4444D);
        getCache().cacheBoolean().put(KEY, true);
        getCache().cacheString().put(KEY, "hello String");
        getCache().cacheObject().put(TEST_MODEL);
        getCache().cacheMultiObject(TestModel.class).put(KEY, TEST_MODEL);
    }

    private void getData() {
        Log.i(TAG, "cacheInteger:" + getCache().cacheInteger().get(KEY, 0));
        Log.i(TAG, "cacheLong:" + getCache().cacheLong().get(KEY, 0L));
        Log.i(TAG, "cacheFloat:" + getCache().cacheFloat().get(KEY, 0F));
        Log.i(TAG, "cacheDouble:" + getCache().cacheDouble().get(KEY, 0D));
        Log.i(TAG, "cacheBoolean:" + getCache().cacheBoolean().get(KEY, false));
        Log.i(TAG, "cacheString:" + getCache().cacheString().get(KEY, null));
        Log.i(TAG, "cacheObject:" + getCache().cacheObject().get(TestModel.class));
        Log.i(TAG, "cacheMultiObject:" + getCache().cacheMultiObject(TestModel.class).get(KEY));
    }

    @Override
    public void onClick(View v) {

    }
}
