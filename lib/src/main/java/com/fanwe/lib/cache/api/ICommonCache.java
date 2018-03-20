package com.fanwe.lib.cache.api;

/**
 * Created by zhengjun on 2018/3/20.
 */
public interface ICommonCache<T>
{
    boolean put(String key, T value);

    T get(String key);
}
