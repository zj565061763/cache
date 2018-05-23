package com.fanwe.lib.cache;

import com.fanwe.lib.cache.api.CommonCache;
import com.fanwe.lib.cache.api.ObjectCache;
import com.fanwe.lib.cache.api.SerializableCache;
import com.fanwe.lib.cache.converter.EncryptConverter;
import com.fanwe.lib.cache.converter.ObjectConverter;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface Disk<T extends Disk>
{
    /**
     * 设置保存缓存的时候是否加密
     *
     * @param encrypt
     * @return
     */
    T setEncrypt(boolean encrypt);

    /**
     * 设置是否支持内存存储
     *
     * @param memorySupport
     * @return
     */
    T setMemorySupport(boolean memorySupport);

    /**
     * 设置加解密转换器
     *
     * @param encryptConverter
     * @return
     */
    T setEncryptConverter(EncryptConverter encryptConverter);

    /**
     * 设置对象转换器
     *
     * @param objectConverter
     * @return
     */
    T setObjectConverter(ObjectConverter objectConverter);

    /**
     * 返回当前目录的大小
     *
     * @return
     */
    long size();

    /**
     * 删除该目录以及目录下的所有缓存
     */
    void delete();

    //---------- cache start ----------

    CommonCache<Integer> cacheInteger();

    CommonCache<Long> cacheLong();

    CommonCache<Float> cacheFloat();

    CommonCache<Double> cacheDouble();

    CommonCache<Boolean> cacheBoolean();

    CommonCache<String> cacheString();

    SerializableCache cacheSerializable();

    ObjectCache cacheObject();

    //---------- cache end ----------
}
