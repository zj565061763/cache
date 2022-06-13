package com.sd.lib.cache.store

import com.sd.lib.cache.Cache.CacheStore
import com.sd.lib.cache.CacheInfo
import com.sd.lib.cache.LibUtils
import java.io.File

/**
 * 文件缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory: File

    private val directory: File?
        get() = if (_directory.exists() || _directory.mkdirs()) {
            _directory
        } else null

    override fun putCache(key: String, value: ByteArray, info: CacheInfo): Boolean {
        val file = getCacheFile(key, info) ?: return false
        return try {
            putCacheImpl(key, value, file)
        } catch (e: Exception) {
            info.exceptionHandler.onException(e)
            false
        }
    }

    override fun getCache(key: String, clazz: Class<*>?, info: CacheInfo): ByteArray? {
        val file = getCacheFile(key, info) ?: return null
        return try {
            getCacheImpl(key, clazz, file)
        } catch (e: Exception) {
            info.exceptionHandler.onException(e)
            null
        }
    }

    override fun removeCache(key: String, info: CacheInfo): Boolean {
        val file = getCacheFile(key, info) ?: return false
        return try {
            removeCacheImpl(key, file)
        } catch (e: Exception) {
            info.exceptionHandler.onException(e)
            false
        }
    }

    override fun containsCache(key: String, info: CacheInfo): Boolean {
        val file = getCacheFile(key, info) ?: return false
        return file.exists()
    }

    @Throws(Exception::class)
    protected abstract fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean

    @Throws(Exception::class)
    protected abstract fun getCacheImpl(key: String, clazz: Class<*>?, file: File): ByteArray

    @Throws(Exception::class)
    protected fun removeCacheImpl(key: String, file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    private fun getCacheFile(key: String, info: CacheInfo): File? {
        if (key.isEmpty()) return null

        val fileKey = transformKey(key)
        if (fileKey.isEmpty()) {
            info.exceptionHandler.onException(RuntimeException("transformKey() return empty"))
            return null
        }

        val dir = directory
        if (dir == null) {
            info.exceptionHandler.onException(RuntimeException("directory is not available:" + _directory.absolutePath))
            return null
        }
        return File(dir, fileKey)
    }

    protected open fun transformKey(key: String): String {
        return LibUtils.md5(key)
    }

    init {
        _directory = directory
    }
}