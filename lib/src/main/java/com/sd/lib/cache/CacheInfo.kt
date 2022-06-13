package com.sd.lib.cache;

public interface CacheInfo {
    /**
     * 是否需要加解密
     *
     * @return
     */
    boolean isEncrypt();

    /**
     * 是否支持内存存储
     *
     * @return
     */
    boolean isMemorySupport();

    /**
     * 返回缓存存取对象
     *
     * @return
     */
    Cache.CacheStore getCacheStore();

    /**
     * 返回对象转换器
     *
     * @return
     */
    Cache.ObjectConverter getObjectConverter();

    /**
     * 返回加解密转换器
     *
     * @return
     */
    Cache.EncryptConverter getEncryptConverter();

    /**
     * 返回设置的异常处理对象
     *
     * @return
     */
    Cache.ExceptionHandler getExceptionHandler();
}
