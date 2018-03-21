package com.fanwe.lib.cache.api;

/**
 * 对象缓存接口
 */
public interface IObjectCache<T>
{
    boolean put(T value);

    T get();

    boolean remove();
}
