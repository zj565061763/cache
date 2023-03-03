package com.sd.lib.cache

interface CacheInfo {
    /** 存取库 */
    val cacheStore: Cache.CacheStore

    /** 对象转换 */
    val objectConverter: Cache.ObjectConverter

    /** 异常处理 */
    val exceptionHandler: Cache.ExceptionHandler
}