package com.sd.www.cache;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;
import com.tencent.mmkv.MMKV;

/**
 * 自定义缓存
 */
public class MMKVCacheStore implements Cache.CacheStore
{
    private final MMKV mMMKV = MMKV.defaultMMKV();

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
