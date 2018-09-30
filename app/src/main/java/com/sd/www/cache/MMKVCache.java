package com.sd.www.cache;

import com.sd.lib.cache.CacheInfo;
import com.sd.lib.cache.FCache;
import com.tencent.mmkv.MMKV;

/**
 * 自定义Cache，底层用腾讯微信的MMKV库实现存储
 */
public class MMKVCache extends FCache
{
    private final MMKV mMMKV = MMKV.defaultMMKV();

    @Override
    public CacheStore getCacheStore()
    {
        return mCacheStore;
    }

    private final CacheStore mCacheStore = new CacheStore()
    {
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
    };
}
