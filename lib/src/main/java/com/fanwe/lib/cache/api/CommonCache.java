package com.fanwe.lib.cache.api;

/**
 * 通用缓存接口
 */
public interface CommonCache<T>
{
    /**
     * 放入缓存对象
     *
     * @param key
     * @param value
     * @return
     */
    boolean put(String key, T value);

    /**
     * 获得缓存对象
     *
     * @param key
     * @return
     */
    T get(String key);

    /**
     * 返回key对应的缓存
     *
     * @param key
     * @param defaultValue 如果获取的缓存为null或者不存在，则返回这个值
     * @return
     */
    T get(String key, T defaultValue);
}
