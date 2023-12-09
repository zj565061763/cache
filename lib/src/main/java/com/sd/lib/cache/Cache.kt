package com.sd.lib.cache

import android.content.Context
import java.io.File

interface Cache {

    fun cacheInt(): CommonCache<Int>
    fun cacheLong(): CommonCache<Long>
    fun cacheFloat(): CommonCache<Float>
    fun cacheDouble(): CommonCache<Double>
    fun cacheBoolean(): CommonCache<Boolean>
    fun cacheString(): CommonCache<String>
    fun cacheBytes(): CommonCache<ByteArray>

    fun <T> objectSingle(clazz: Class<T>): SingleObjectCache<T>
    fun <T> objectMulti(clazz: Class<T>): MultiObjectCache<T>

    /**
     * 基本数据类型，通用缓存接口
     */
    interface CommonCache<T> {
        /**
         * 保存缓存
         * @return true-保存成功，false-保存失败
         */
        fun put(key: String, value: T?): Boolean

        /**
         * 获取[key]对应的缓存
         */
        fun get(key: String): T?

        /**
         * 移除[key]对应的缓存
         */
        fun remove(key: String)

        /**
         * [key]对应的缓存是否存在
         */
        fun contains(key: String): Boolean
    }

    /**
     * 单对象缓存接口
     */
    interface SingleObjectCache<T> {
        fun put(value: T?): Boolean
        fun get(): T?
        fun remove()
        fun contains(): Boolean
    }

    /**
     * 多对象缓存接口
     */
    interface MultiObjectCache<T> {
        fun put(key: String, value: T?): Boolean
        fun get(key: String): T?
        fun remove(key: String)
        fun contains(key: String): Boolean
    }

    interface CacheStore {
        /**
         * 初始化
         */
        fun init(context: Context, directory: File)

        /**
         * 保存缓存
         * @return true-保存成功，false-保存失败
         */
        @Throws(Exception::class)
        fun putCache(key: String, value: ByteArray): Boolean

        /**
         * 获取缓存
         */
        @Throws(Exception::class)
        fun getCache(key: String): ByteArray?

        /**
         * 删除缓存
         */
        @Throws(Exception::class)
        fun removeCache(key: String)

        /**
         * 是否有[key]对应的缓存
         */
        @Throws(Exception::class)
        fun containsCache(key: String): Boolean
    }

    /**
     * 对象转换器
     */
    interface ObjectConverter {
        /**
         * 编码
         */
        @Throws(Exception::class)
        fun encode(value: Any, clazz: Class<*>): ByteArray

        /**
         * 解码
         */
        @Throws(Exception::class)
        fun <T> decode(bytes: ByteArray, clazz: Class<T>): T
    }

    /**
     * 异常处理类
     */
    fun interface ExceptionHandler {
        fun onException(e: Exception)
    }
}

internal interface CacheInfo {
    /** 仓库 */
    val cacheStore: Cache.CacheStore

    /** 对象转换 */
    val objectConverter: Cache.ObjectConverter

    /** 异常处理 */
    val exceptionHandler: Cache.ExceptionHandler
}

class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : Exception(message, cause)