package com.fanwe.lib.cache.api;

/**
 * 通用缓存接口
 */
public interface ICommonCache<T>
{
    boolean put(String key, T value);

    T get(String key);
}
