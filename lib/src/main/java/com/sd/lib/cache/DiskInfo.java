package com.sd.lib.cache;

import java.io.File;

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
    Disk.EncryptConverter getEncryptConverter();

    /**
     * 返回对象转换器
     *
     * @return
     */
    Disk.ObjectConverter getObjectConverter();

    /**
     * 返回设置的异常处理对象
     *
     * @return
     */
    Disk.ExceptionHandler getExceptionHandler();
}
