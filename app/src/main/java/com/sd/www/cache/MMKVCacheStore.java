package com.sd.www.cache;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;
import com.tencent.mmkv.MMKV;

/**
 * 自定义CacheStore
 */
public class MMKVCacheStore implements Cache.CacheStore {
    private final MMKV _mmkv;

    public MMKVCacheStore(Context context) {
        MMKV.initialize(context);
        _mmkv = MMKV.defaultMMKV();
    }

    @Override
    public boolean putCache(@NonNull String key, @NonNull byte[] value, @NonNull CacheInfo info) {
        return _mmkv.encode(key, value);
    }

    @Override
    public byte[] getCache(@NonNull String key, @NonNull CacheInfo info) {
        return _mmkv.decodeBytes(key);
    }

    @Override
    public boolean removeCache(@NonNull String key, @NonNull CacheInfo info) {
        _mmkv.remove(key);
        return true;
    }

    @Override
    public boolean containsCache(@NonNull String key, @NonNull CacheInfo info) {
        return _mmkv.contains(key);
    }
}
