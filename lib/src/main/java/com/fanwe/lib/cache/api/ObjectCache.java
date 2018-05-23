package com.fanwe.lib.cache.api;

/**
 * 对象缓存接口
 */
public interface ObjectCache
{
    boolean put(Object value);

    <T> T get(Class<T> clazz);

    boolean remove(Class clazz);
}
