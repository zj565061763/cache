package com.fanwe.lib.cache;

import com.fanwe.lib.cache.converter.EncryptConverter;
import com.fanwe.lib.cache.converter.ObjectConverter;

import java.io.File;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface DiskInfo
{
    /**
     * 是否需要加解密
     *
     * @return
     */
    boolean isEncrypt();

    /**
     * 是否支持内存存取
     *
     * @return
     */
    boolean isMemorySupport();

    /**
     * 返回存取的目录
     *
     * @return
     */
    File getDirectory();

    /**
     * 返回加解密转换器
     *
     * @return
     */
    EncryptConverter getEncryptConverter();

    /**
     * 返回对象转换器
     *
     * @return
     */
    ObjectConverter getObjectConverter();
}
