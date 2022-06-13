package com.sd.lib.cache.store

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import java.io.File
import java.security.MessageDigest

/**
 * 文件缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory: File

    private fun getDirectory(): File? {
        return if (_directory.exists() || _directory.mkdirs()) {
            _directory
        } else null
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        val file = getCacheFile(key) ?: return false
        return putCacheImpl(key, value, file)
    }

    final override fun getCache(key: String): ByteArray? {
        val file = getCacheFile(key) ?: return null
        return getCacheImpl(key, file)
    }

    final override fun removeCache(key: String): Boolean {
        val file = getCacheFile(key) ?: return false
        return removeCacheImpl(key, file)
    }

    final override fun containsCache(key: String): Boolean {
        val file = getCacheFile(key) ?: return false
        return containsCacheImpl(key, file)
    }

    @Throws(Exception::class)
    protected abstract fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean

    @Throws(Exception::class)
    protected abstract fun getCacheImpl(key: String, file: File): ByteArray

    @Throws(Exception::class)
    protected open fun removeCacheImpl(key: String, file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    @Throws(Exception::class)
    protected open fun containsCacheImpl(key: String, file: File): Boolean {
        return file.exists()
    }

    @Throws(Exception::class)
    private fun getCacheFile(key: String): File? {
        if (key.isEmpty()) return null

        val transformKey = transformKey(key)
        if (transformKey.isEmpty()) {
            throw RuntimeException("transformKey() return empty")
        }

        val dir = getDirectory() ?: throw RuntimeException("directory is not available:" + _directory.absolutePath)
        return File(dir, KeyPrefix + transformKey)
    }

    @Throws(Exception::class)
    protected open fun transformKey(key: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }
    }

    /**
     * 返回所有缓存的文件key
     */
    protected fun getFileKeys(): List<String> {
        synchronized(Cache::class.java) {
            try {
                val list = getDirectory()?.list()
                if (list != null) {
                    return list.filter { it.startsWith(KeyPrefix) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return listOf()
        }
    }

    /**
     * 根据文件key移除缓存
     */
    protected fun removeFileKey(key: String) {
        if (!key.startsWith(KeyPrefix)) return
        synchronized(Cache::class.java) {
            try {
                val dir = getDirectory() ?: return
                File(dir, key).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        _directory = directory
    }

    companion object {
        private const val KeyPrefix = "f_d_"
    }
}