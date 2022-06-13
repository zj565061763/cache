package com.sd.lib.cache.store

import com.sd.lib.cache.Cache.CacheStore
import java.io.File
import java.security.MessageDigest

/**
 * 文件缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory: File

    private val directory: File?
        get() = if (_directory.exists() || _directory.mkdirs()) {
            _directory
        } else null

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

        val fileKey = transformKey(key)
        if (fileKey.isEmpty()) {
            throw RuntimeException("transformKey() return empty")
        }

        val dir = directory ?: throw RuntimeException("directory is not available:" + _directory.absolutePath)
        return File(dir, fileKey)
    }

    @Throws(Exception::class)
    protected open fun transformKey(key: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
        return bytes.joinToString("") { "%02X".format(it) }
    }

    init {
        _directory = directory
    }
}