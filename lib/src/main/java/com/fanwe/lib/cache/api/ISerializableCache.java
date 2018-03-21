package com.fanwe.lib.cache.api;

import java.io.Serializable;

/**
 * Created by zhengjun on 2018/3/21.
 */
public interface ISerializableCache
{
     boolean put(Serializable value);

    <T extends Serializable> T get(Class<T> clazz);

    boolean remove();
}
