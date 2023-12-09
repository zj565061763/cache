package com.sd.lib.cache.store

import com.sd.lib.cache.Cache.CacheStore
import java.io.File
import java.security.MessageDigest

/**
 * 磁盘缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory = directory

    //---------- CacheStore start ----------

    final override fun putCache(key: String, value: ByteArray): Boolean {
        val file = getCacheFile(key)
        return putCacheImpl(key, value, file)
    }

    final override fun getCache(key: String): ByteArray? {
        val file = getCacheFile(key)
        if (!file.exists()) return null
        return getCacheImpl(key, file)
    }

    final override fun removeCache(key: String) {
        val file = getCacheFile(key)
        if (!file.exists()) return
        removeCacheImpl(key, file)
    }

    final override fun containsCache(key: String): Boolean {
        val file = getCacheFile(key)
        return containsCacheImpl(key, file)
    }

    //---------- CacheStore end ----------

    //---------- Impl start ----------

    @Throws(Exception::class)
    protected abstract fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean

    @Throws(Exception::class)
    protected abstract fun getCacheImpl(key: String, file: File): ByteArray?

    @Throws(Exception::class)
    protected open fun removeCacheImpl(key: String, file: File): Boolean {
        return file.delete()
    }

    @Throws(Exception::class)
    protected open fun containsCacheImpl(key: String, file: File): Boolean {
        return file.exists()
    }

    //---------- Impl end ----------

    private fun getDirectory(): File {
        if (checkDir(_directory)) {
            return _directory
        } else {
            error("directory is not available:${_directory.absolutePath}")
        }
    }

    private fun getCacheFile(key: String): File {
        val dir = getDirectory()
        return File(dir, transformKey(key))
    }

    companion object {
        private const val KeyPrefix = "fdc_"

        private fun transformKey(key: String): String {
            return KeyPrefix + md5(key)
        }

        private fun md5(key: String): String {
            val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
            return bytes.joinToString("") { "%02X".format(it) }
        }

        private fun checkDir(file: File): Boolean {
            if (!file.exists()) return file.mkdirs()
            if (file.isDirectory) return true
            file.delete()
            return file.mkdirs()
        }
    }
}