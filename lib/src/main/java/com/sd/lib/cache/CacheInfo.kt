package com.sd.lib.cache


interface CacheInfo {
    /** 存取对象 */
    val cacheStore: Cache.CacheStore

    /** 是否需要加解密 */
    val isEncrypt: Boolean

    /** 对象转换器 */
    val objectConverter: Cache.ObjectConverter

    /** 加解密转换器 */
    val encryptConverter: Cache.EncryptConverter?

    /** 异常处理对象 */
    val exceptionHandler: Cache.ExceptionHandler
}