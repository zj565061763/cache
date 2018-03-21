package com.fanwe.lib.cache.api;

import java.io.Serializable;

/**
 * 序列化缓存接口
 */
public interface ISerializableCache
{
    <T extends Serializable> boolean put(T value);

    <T extends Serializable> T get(Class<T> clazz);

    <T extends Serializable> boolean remove(Class<T> clazz);
}
