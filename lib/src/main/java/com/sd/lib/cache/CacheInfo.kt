package com.sd.lib.cache

import com.sd.lib.cache.Cache.*

interface CacheInfo {
    /** 是否需要加解密 */
    val isEncrypt: Boolean

    /** 是否支持内存存储 */
    val isMemorySupport: Boolean

    /** 存取对象 */
    val cacheStore: CacheStore

    /** 对象转换器 */
    val objectConverter: ObjectConverter?

    /** 加解密转换器 */
    val encryptConverter: EncryptConverter?

    /** 异常处理对象 */
    val exceptionHandler: Cache.ExceptionHandler?
}