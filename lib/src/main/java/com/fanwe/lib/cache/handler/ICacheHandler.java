package com.fanwe.lib.cache.handler;

/**
 * Created by zhengjun on 2018/3/20.
 */
public interface ICacheHandler<T>
{
    boolean putCache(String key, T value);

    T getCache(String key, Class<T> clazz);

    boolean removeCache(String key);
}
