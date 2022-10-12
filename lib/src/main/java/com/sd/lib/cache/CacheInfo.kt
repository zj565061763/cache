package com.sd.lib.cache

interface CacheInfo {
    /** 存取对象 */
    val cacheStore: Cache.CacheStore

    /** 对象转换器 */
    val objectConverter: Cache.ObjectConverter

    /** 异常处理对象 */
    val exceptionHandler: Cache.ExceptionHandler
}