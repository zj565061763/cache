package com.sd.lib.cache

interface Cache {
    /**
     * 是否加密
     */
    fun setEncrypt(encrypt: Boolean): Cache

    /**
     * 是否缓存到内存
     */
    fun setMemorySupport(support: Boolean): Cache

    /**
     * 设置对象转换器
     */
    fun setObjectConverter(converter: ObjectConverter?): Cache

    /**
     * 设置加解密转换器
     */
    fun setEncryptConverter(converter: EncryptConverter?): Cache

    /**
     * 设置异常处理器
     */
    fun setExceptionHandler(handler: ExceptionHandler?): Cache

    //---------- cache start ----------

    fun cacheInteger(): CommonCache<Int>
    fun cacheLong(): CommonCache<Long>
    fun cacheFloat(): CommonCache<Float>
    fun cacheDouble(): CommonCache<Double>
    fun cacheBoolean(): CommonCache<Boolean>
    fun cacheString(): CommonCache<String>
    fun cacheBytes(): CommonCache<ByteArray>

    fun cacheObject(): ObjectCache
    fun <T> cacheMultiObject(clazz: Class<T>): MultiObjectCache<T>

    //---------- cache end ----------

    /**
     * 通用缓存接口
     */
    interface CommonCache<T> {
        /**
         * 放入缓存对象
         * @return true-成功；false-失败
         */
        fun put(key: String, value: T): Boolean

        /**
         * 获取[key]对应的缓存
         * @param defaultValue 如果获取的缓存不存在，则返回这个值
         */
        fun get(key: String, defaultValue: T): T

        /**
         * 移除[key]对应的缓存
         */
        fun remove(key: String): Boolean

        /**
         * [key]对应的缓存是否存在
         */
        fun contains(key: String): Boolean
    }

    /**
     * 对象缓存接口
     */
    interface ObjectCache {
        fun put(value: Any?): Boolean
        fun <T> get(clazz: Class<T>): T?
        fun remove(clazz: Class<*>): Boolean
        fun contains(clazz: Class<*>): Boolean
    }

    interface MultiObjectCache<T> {
        fun put(key: String, value: T?): Boolean
        fun get(key: String): T?
        fun remove(key: String): Boolean
        fun contains(key: String): Boolean
    }

    interface CacheStore {
        /**
         * 保存缓存
         * @return true-保存成功，false-保存失败
         */
        @Throws(Exception::class)
        fun putCache(key: String, value: ByteArray, info: CacheInfo): Boolean

        /**
         * 获取缓存
         */
        @Throws(Exception::class)
        fun getCache(key: String, info: CacheInfo): ByteArray?

        /**
         * 删除缓存
         * @return true-缓存被删除，false-删除失败或者缓存不存在
         */
        @Throws(Exception::class)
        fun removeCache(key: String, info: CacheInfo): Boolean

        /**
         * 是否有[key]对应的缓存
         */
        @Throws(Exception::class)
        fun containsCache(key: String, info: CacheInfo): Boolean
    }

    /**
     * 对象转换器
     */
    interface ObjectConverter {
        /**
         * 对象转byte
         */
        @Throws(Exception::class)
        fun objectToByte(value: Any): ByteArray

        /**
         * byte转对象
         */
        @Throws(Exception::class)
        fun <T> byteToObject(bytes: ByteArray, clazz: Class<T>): T
    }

    /**
     * 加解密转换器
     */
    interface EncryptConverter {
        /**
         * 加密数据
         */
        @Throws(Exception::class)
        fun encrypt(bytes: ByteArray): ByteArray

        /**
         * 解密数据
         */
        @Throws(Exception::class)
        fun decrypt(bytes: ByteArray): ByteArray
    }

    /**
     * 异常处理类
     */
    fun interface ExceptionHandler {
        fun onException(e: Exception)
    }
}