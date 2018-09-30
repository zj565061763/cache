package com.sd.lib.cache.store;

import com.sd.lib.cache.Cache;
import com.sd.lib.cache.CacheInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存
 */
public class SimpleMemoryCacheStore implements Cache.CacheStore
{
    private final Map<String, byte[]> MAP_CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean putCache(String key, byte[] value, CacheInfo info)
    {
        MAP_CACHE.put(key, value);
        return true;
    }

    @Override
    public byte[] getCache(String key, Class clazz, CacheInfo info)
    {
        return MAP_CACHE.get(key);
    }

    @Override
    public boolean removeCache(String key, CacheInfo info)
    {
        return MAP_CACHE.remove(key) != null;
    }
}
