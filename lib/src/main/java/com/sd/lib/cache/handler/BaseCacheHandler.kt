package com.sd.lib.cache.handler

import android.text.TextUtils
import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.Cache.CommonCache
import com.sd.lib.cache.CacheInfo

/**
 * 缓存处理基类
 */
internal abstract class BaseCacheHandler<T>(
    val cacheInfo: CacheInfo,
) : CacheHandler<T>, CommonCache<T> {

    /** key前缀 */
    protected abstract val keyPrefix: String

    private fun transformKey(key: String): String {
        require(!TextUtils.isEmpty(key)) { "key is null or empty" }
        val prefix = keyPrefix
        check(!TextUtils.isEmpty(prefix)) { "key prefix is null or empty" }
        return prefix + key
    }

    private val cacheStore: CacheStore
        get() = cacheInfo.cacheStore

    //---------- CacheHandler start ----------

    override fun putCache(key: String, value: T?): Boolean {
        if (value == null) return false
        synchronized(Cache::class.java) {
            val key = transformKey(key)
            val data = transformValueToByte(key, value) ?: return false
            return try {
                cacheStore.putCache(key, data, cacheInfo)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                return false
            }.also {
                if (it) {
                    putMemory(key, data)
                }
            }
        }
    }

    override fun getCache(key: String, clazz: Class<*>?): T? {
        synchronized(Cache::class.java) {
            val key = transformKey(key)
            var data = getMemory(key)
            if (data == null) {
                data = try {
                    cacheStore.getCache(key, cacheInfo)
                } catch (e: Exception) {
                    cacheInfo.exceptionHandler.onException(e)
                    return null
                }
            }
            if (data == null) return null
            return transformByteToValue(key, data, clazz)
        }
    }

    override fun removeCache(key: String): Boolean {
        synchronized(Cache::class.java) {
            val key = transformKey(key)
            removeMemory(key)
            return try {
                cacheStore.removeCache(key, cacheInfo)
            } catch (e: Exception) {
                cacheInfo.exceptionHandler.onException(e)
                false
            }
        }
    }

    override fun containsCache(key: String): Boolean {
        synchronized(Cache::class.java) {
            val key = transformKey(key)
            return if (getMemory(key) != null) {
                true
            } else {
                try {
                    cacheStore.containsCache(key, cacheInfo)
                } catch (e: Exception) {
                    cacheInfo.exceptionHandler.onException(e)
                    false
                }
            }
        }
    }

    //---------- CacheHandler end ----------

    //---------- Memory start ----------

    private fun putMemory(key: String, value: ByteArray) {
        if (cacheInfo.isMemorySupport) {
            MAP_MEMORY[key] = value
        }
    }

    private fun getMemory(key: String): ByteArray? {
        return if (cacheInfo.isMemorySupport) MAP_MEMORY[key] else null
    }

    private fun removeMemory(key: String) {
        MAP_MEMORY.remove(key)
    }

    //---------- Memory end ----------

    //---------- CommonCache start ----------

    override fun put(key: String, value: T): Boolean {
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
        dataWithTag[dataWithTag.size - 1] = (if (isEncrypt) 1 else 0).toByte()
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
    protected abstract fun byteToValue(bytes: ByteArray?, clazz: Class<*>?): T

    companion object {
        private val MAP_MEMORY = mutableMapOf<String, ByteArray>()
    }
}