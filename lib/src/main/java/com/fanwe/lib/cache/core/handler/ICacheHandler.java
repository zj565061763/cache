package com.fanwe.lib.cache.core.handler;

/**
 * Created by zhengjun on 2018/3/20.
 */
interface ICacheHandler<T>
{
    boolean putCache(String key, T value);

    T getCache(String key, Class clazz);

    boolean hasCache(String key);

    boolean removeCache(String key);
}
