package com.fanwe.lib.cache.handler;

/**
 * 缓存处理接口
 */
public interface ICacheHandler<T>
{
    boolean putCache(String key, T value);

    T getCache(String key, Class<T> clazz);

    boolean removeCache(String key);
}
