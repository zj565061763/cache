package com.sd.lib.cache.store

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import java.io.File
import java.security.MessageDigest

/**
 * 磁盘缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory = directory

    //---------- CacheStore start ----------

    @Throws(Exception::class)
    final override fun putCache(key: String, value: ByteArray): Boolean {
        val file = getCacheFile(key)
        return putCacheImpl(key, value, file)
    }

    @Throws(Exception::class)
    final override fun getCache(key: String): ByteArray? {
        val file = getCacheFile(key)
        if (!file.exists()) return null
        return getCacheImpl(key, file)
    }

    @Throws(Exception::class)
    final override fun removeCache(key: String): Boolean {
        val file = getCacheFile(key)
        if (!file.exists()) return false
        return removeCacheImpl(key, file)
    }

    @Throws(Exception::class)
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

    //---------- Impl End ----------

    @Throws(Exception::class)
    private fun getDirectory(): File {
        return if (_directory.exists() || _directory.mkdirs()) {
            _directory
        } else {
            throw RuntimeException("directory is not available:" + _directory.absolutePath)
        }
    }

    @Throws(Exception::class)
    private fun getCacheFile(key: String): File {
        return File(getDirectory(), transformKey(key))
    }

    /**
     * 返回所有缓存的文件
     */
    fun getCacheFiles(): Array<File>? {
        synchronized(Cache::class.java) {
            return try {
                getDirectory().listFiles { file ->
                    file.name.startsWith(KeyPrefix) && file.isFile
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 移除文件名对应的缓存
     */
    fun removeCacheByFilename(filename: String) {
        synchronized(Cache::class.java) {
            try {
                val file = File(getDirectory(), filename)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(Exception::class)
    fun transformKey(key: String): String {
        return KeyPrefix + md5(key)
    }

    companion object {
        private const val KeyPrefix = "f_d_"

        private fun md5(key: String): String {
            val bytes = MessageDigest.getInstance("MD5").digest(key.toByteArray())
            return bytes.joinToString("") { "%02X".format(it) }
        }
    }
}