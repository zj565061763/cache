package com.sd.lib.cache.handler;

import com.sd.lib.cache.DiskInfo;

public interface RealCacheHandler
{
    /**
     * 保存缓存
     *
     * @param key
     * @param value
     * @param info
     * @return true-保存成功，false-保存失败
     */
    boolean putCache(String key, byte[] value, DiskInfo info);

    /**
     * 获得缓存
     *
     * @param key
     * @param clazz
     * @param info
     * @return
     */
    byte[] getCache(String key, Class clazz, DiskInfo info);

    /**
     * 删除缓存
     *
     * @param key
     * @param info
     * @return true-此次方法调用后删除了缓存，false-删除失败或者缓存不存在
     */
    boolean removeCache(String key, DiskInfo info);
}
