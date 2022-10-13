package com.sd.demo.cache;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheConfig;
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
    }

    private Cache getCache() {
        if (mCache == null) {
            /**
             * 使用本地磁盘缓存，
             * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置{@link CacheConfig.Builder#setCacheStore(Cache.CacheStore)}
             */
            mCache = FCache.disk();
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
        getCache().cacheMultiObject(TestModel.class).put(KEY + KEY, TEST_MODEL);
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
        Log.i(TAG, "cacheMultiObject:" + getCache().cacheMultiObject(TestModel.class).get(KEY + KEY));
    }

    private void removeData() {
        getCache().cacheInteger().remove(KEY);
        getCache().cacheLong().remove(KEY);
        getCache().cacheFloat().remove(KEY);
        getCache().cacheDouble().remove(KEY);
        getCache().cacheBoolean().remove(KEY);
        getCache().cacheString().remove(KEY);
        getCache().cacheObject().remove(TestModel.class);
        getCache().cacheMultiObject(TestModel.class).remove(KEY);
        getCache().cacheMultiObject(TestModel.class).remove(KEY + KEY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_put:
                putData();
                break;
            case R.id.btn_get:
                getData();
                break;
            case R.id.btn_remove:
                removeData();
                break;
            default:
                break;
        }
    }
}
