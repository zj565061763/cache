package com.sd.demo.cache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sd.lib.cache.store.lru.LruCacheStore;
import com.tencent.mmkv.MMKV;

import java.util.Arrays;
import java.util.List;

public class MMKVLruCacheStore extends LruCacheStore {
    private final MMKV _mmkv;

    public MMKVLruCacheStore(Context context, int maxSize) {
        super(maxSize);
        MMKV.initialize(context, context.getExternalCacheDir().getAbsolutePath() + "/mmkv_test");
        _mmkv = MMKV.defaultMMKV();
    }

    @Override
    protected boolean putCacheImpl(@NonNull String key, @NonNull byte[] value) {
        return _mmkv.encode(key, value);
    }

    @Nullable
    @Override
    protected byte[] getCacheImpl(@NonNull String key) {
        return _mmkv.decodeBytes(key);
    }

    @Override
    protected boolean removeCacheImpl(@NonNull String key) {
        _mmkv.removeValueForKey(key);
        return true;
    }

    @Override
    protected boolean containsCacheImpl(@NonNull String key) {
        return _mmkv.containsKey(key);
    }

    @Nullable
    @Override
    protected List<String> getLruCacheInitKeys() {
        final String[] keys = _mmkv.allKeys();
        if (keys == null) return null;
        return Arrays.asList(keys);
    }

    @Override
    protected int sizeOfLruCacheEntry(@NonNull String key) {
        return 1;
    }

    @Override
    protected void onLruCacheEntryEvicted(@NonNull String key) throws Exception {
        _mmkv.removeValueForKey(key);
    }
}
