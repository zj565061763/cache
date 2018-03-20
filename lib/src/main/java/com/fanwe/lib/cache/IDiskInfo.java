package com.fanwe.lib.cache;

import com.fanwe.lib.cache.converter.IEncryptConverter;
import com.fanwe.lib.cache.converter.IObjectConverter;

import java.io.File;

/**
 * Created by zhengjun on 2017/9/1.
 */
public interface IDiskInfo
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
    IEncryptConverter getEncryptConverter();

    /**
     * 返回对象转换器
     *
     * @return
     */
    IObjectConverter getObjectConverter();
}
