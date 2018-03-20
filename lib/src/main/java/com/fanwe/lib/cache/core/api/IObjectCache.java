package com.fanwe.lib.cache.core.api;

/**
 * Created by zhengjun on 2018/3/20.
 */
public interface IObjectCache<T>
{
    boolean put(T value);

    T get(Class<T> clazz);

    boolean remove(Class<T> clazz);
}
