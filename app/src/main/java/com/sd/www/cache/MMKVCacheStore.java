package com.sd.www.cache;

import android.content.Context;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;
import com.tencent.mmkv.MMKV;

/**
 * 自定义CacheStore
 */
public class MMKVCacheStore implements Cache.CacheStore
{
    private final MMKV mMMKV;

    public MMKVCacheStore(Context context)
    {
        MMKV.initialize(context);
        mMMKV = MMKV.defaultMMKV();
    }

    @Override
    public boolean putCache(String key, byte[] value, CacheInfo info)
    {
        return mMMKV.encode(key, value);
    }

    @Override
    public byte[] getCache(String key, Class clazz, CacheInfo info)
    {
        return mMMKV.decodeBytes(key);
    }

    @Override
    public boolean removeCache(String key, CacheInfo info)
    {
        mMMKV.remove(key);
        return true;
    }
}
