package com.sd.lib.cache

interface Cache {

    fun <T> o(clazz: Class<T>): SingleObjectCache<T>
    fun <T> oo(clazz: Class<T>): MultiObjectCache<T>

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