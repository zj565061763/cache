package com.sd.lib.cache.handler;

/**
 * 缓存处理接口
 */
interface CacheHandler<T>
{
    boolean putCache(String key, T value);

    T getCache(String key, Class clazz);

    boolean removeCache(String key);

    boolean containsCache(String key);
}
