package com.sd.demo.cache;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sd.lib.cache.Cache;
import com.tencent.mmkv.MMKV;

/**
 * 自定义CacheStore。
 * <p>
 * MMKV有个bug，如果存的是空byte[]数组，取的时候会返回null，正常返回应该也是个空的byte[]数组。
 */
public class MMKVCacheStore implements Cache.CacheStore {
    private final MMKV _mmkv;

    public MMKVCacheStore(Context context) {
        MMKV.initialize(context);
        _mmkv = MMKV.defaultMMKV();
    }

    @Override
    public boolean putCache(@NonNull String key, @NonNull byte[] value) {
        return _mmkv.encode(key, value);
    }

    @Override
    public byte[] getCache(@NonNull String key) {
        return _mmkv.decodeBytes(key);
    }

    @Override
    public boolean removeCache(@NonNull String key) {
        _mmkv.remove(key);
        return true;
    }

    @Override
    public boolean containsCache(@NonNull String key) {
        return _mmkv.contains(key);
    }
}
