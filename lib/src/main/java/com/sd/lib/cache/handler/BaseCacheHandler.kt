package com.sd.lib.cache.handler

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.exception.CacheException

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
    val keyPrefix: String,
) : CacheHandler<T>, CommonCache<T> {

    private val _cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    init {
        require(keyPrefix.isNotEmpty()) { "keyPrefix is empty" }
    }

    //---------- CommonCache start ----------

    final override fun put(key: String, value: T?): Boolean {
        return putCache(key, value)
    }

    final override operator fun get(key: String, defaultValue: T): T {
        return getCache(key, null) ?: defaultValue
    }

    final override fun remove(key: String): Boolean {
        return removeCache(key)
    }

    final override operator fun contains(key: String): Boolean {
        return containsCache(key)
    }

    //---------- CommonCache end ----------

    private fun transformKey(key: String): String {
        require(key.isNotEmpty()) { "key is empty" }
        return keyPrefix + "_" + key
    }

    private fun notifyException(e: Exception) {
        if (e is CacheException) throw e
        cacheInfo.exceptionHandler.onException(e)
    }

    //---------- CacheHandler start ----------

    final override fun putCache(key: String, value: T?): Boolean {
        if (value == null) return false
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                encodeToByte(key, value).let { data ->
                    _cacheStore.putCache(key, data)
                }
            } catch (e: Exception) {
                notifyException(e)
                false
            }
        }
    }

    final override fun getCache(key: String, clazz: Class<*>?): T? {
        val key = transformKey(key)
        synchronized(Cache::class.java) {
            return try {
                _cacheStore.getCache(key)?.let { data ->
                    decodeFromByte(key, data, clazz)
                }
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                null
            }
        }
    }

    final override fun removeCache(key: String): Boolean {
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

    final override fun containsCache(key: String): Boolean {
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

    @Throws(Exception::class)
    private fun encodeToByte(key: String, value: T): ByteArray {
        var data = valueToByte(value)

        val isEncrypt = cacheInfo.isEncrypt
        if (isEncrypt) {
            val converter = cacheInfo.encryptConverter ?: throw CacheException("EncryptConverter is null. key:$key")
            data = converter.encrypt(data)
        }

        val dataWithTag = data.copyOf(data.size + 1)
        dataWithTag[dataWithTag.lastIndex] = (if (isEncrypt) 1 else 0).toByte()
        return dataWithTag
    }

    private fun decodeFromByte(key: String, data: ByteArray, clazz: Class<*>?): T? {
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