package com.sd.lib.cache

interface Cache {

    fun cInt(): CommonCache<Int>
    fun cLong(): CommonCache<Long>
    fun cFloat(): CommonCache<Float>
    fun cDouble(): CommonCache<Double>
    fun cBoolean(): CommonCache<Boolean>
    fun cString(): CommonCache<String>
    fun cBytes(): CommonCache<ByteArray>

    fun <T> cObject(clazz: Class<T>): SingleObjectCache<T>
    fun <T> cObjects(clazz: Class<T>): MultiObjectCache<T>

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
        fun keys(): Array<String>
    }

    /**
     * 对象转换器
     */
    interface ObjectConverter {
        /**
         * 编码
         */
        @Throws(Throwable::class)
        fun <T> encode(value: T, clazz: Class<T>): ByteArray

        /**
         * 解码
         */
        @Throws(Throwable::class)
        fun <T> decode(bytes: ByteArray, clazz: Class<T>): T
    }

    /**
     * 异常处理类
     */
    fun interface ExceptionHandler {
        fun onException(error: Throwable)
    }
}

class CacheException(
    message: String = "",
    cause: Throwable? = null,
) : Exception(message, cause)