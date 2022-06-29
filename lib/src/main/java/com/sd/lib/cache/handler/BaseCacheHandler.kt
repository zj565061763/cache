package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.CacheInfo

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
    val keyPrefix: String,
) : CacheHandler<T>, CommonCache<T> {

    init {
        require(keyPrefix.isNotEmpty()) { "keyPrefix is empty" }
    }

    //---------- CommonCache start ----------

    override fun put(key: String, value: T?): Boolean {
        return putCache(key, value)
    }

    override operator fun get(key: String, defaultValue: T): T {
        return getCache(key, null) ?: defaultValue
    }

    override fun remove(key: String): Boolean {
        return removeCache(key)
    }

    override operator fun contains(key: String): Boolean {
        return containsCache(key)
    }

    //---------- CommonCache end ----------

    private fun transformKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return keyPrefix + "_" + key
    }

    private val _cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    //---------- CacheHandler start ----------

    override fun putCache(key: String, value: T?): Boolean {
        if (value == null) return false
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            val data = transformValueToByte(key, value) ?: return false
            return try {
                _cacheStore.putCache(key, data)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                false
            }
        }
    }

    override fun getCache(key: String, clazz: Class<*>?): T? {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            val data = try {
                _cacheStore.getCache(key)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                null
            } ?: return null
            return transformByteToValue(key, data, clazz)
        }
    }

    override fun removeCache(key: String): Boolean {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.removeCache(key)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                false
            }
        }
    }

    override fun containsCache(key: String): Boolean {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.containsCache(key)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                false
            }
        }
    }

    //---------- CacheHandler end ----------

    private fun transformValueToByte(key: String, value: T): ByteArray? {
        var data = try {
            valueToByte(value)
        } catch (e: Exception) {
            cacheInfo.exceptionHandler.onException(e)
            return null
        }

        val isEncrypt = cacheInfo.isEncrypt
        if (isEncrypt) {
            val converter = checkNotNull(cacheInfo.encryptConverter) { "EncryptConverter is null. key:$key" }
            data = try {
                converter.encrypt(data)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                return null
            }
        }

        val dataWithTag = data.copyOf(data.size + 1)
        dataWithTag[dataWithTag.lastIndex] = (if (isEncrypt) 1 else 0).toByte()
        return dataWithTag
    }

    private fun transformByteToValue(key: String, data: ByteArray, clazz: Class<*>?): T? {
        if (data.isEmpty()) {
            cacheInfo.exceptionHandler.onException(RuntimeException("Data is empty. key:$key"))
            return null
        }

        val isEncrypted = data.last().toInt() == 1
        var data = data.copyOf(data.size - 1)

        if (isEncrypted) {
            val converter = checkNotNull(cacheInfo.encryptConverter) { "EncryptConverter is null. key:$key" }
            data = try {
                converter.decrypt(data)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                return null
            }
        }

        return try {
            byteToValue(data, clazz)
        } catch (e: Exception) {
            cacheInfo.exceptionHandler.onException(e)
            return null
        }
    }
    /**
     * 缓存转byte
     */
    @Throws(Exception::class)
    protected abstract fun valueToByte(value: T): ByteArray

    /**
     * byte转缓存
     */
    @Throws(Exception::class)
    protected abstract fun byteToValue(bytes: ByteArray, clazz: Class<*>?): T
}