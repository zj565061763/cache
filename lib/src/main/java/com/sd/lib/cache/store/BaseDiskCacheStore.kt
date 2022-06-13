package com.sd.lib.cache.store

import com.sd.lib.cache.Cache
import com.sd.lib.cache.Cache.CacheStore
import java.io.File

/**
 * 文件缓存
 */
abstract class BaseDiskCacheStore(directory: File) : CacheStore {
    private val _directory = directory

    //---------- CacheStore start ----------

    @Throws(Exception::class)
    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(key, value, getCacheFile(key))
    }

    @Throws(Exception::class)
    final override fun getCache(key: String): ByteArray {
        return getCacheImpl(key, getCacheFile(key))
    }

    @Throws(Exception::class)
    final override fun removeCache(key: String): Boolean {
        return removeCacheImpl(key, getCacheFile(key))
    }

    @Throws(Exception::class)
    final override fun containsCache(key: String): Boolean {
        return containsCacheImpl(key, getCacheFile(key))
    }

    //---------- CacheStore end ----------

    //---------- Impl start ----------

    @Throws(Exception::class)
    protected abstract fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean

    @Throws(Exception::class)
    protected abstract fun getCacheImpl(key: String, file: File): ByteArray

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
        return File(getDirectory(), packKey(key))
    }

    /**
     * 返回[key]对应的缓存
     */
    fun getCacheFileOrNull(key: String): File? {
        synchronized(Cache::class.java) {
            return try {
                val file = File(getDirectory(), packKey(key))
                if (file.exists()) file else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 返回所有缓存的文件
     */
    fun getCacheFiles(): List<File> {
        synchronized(Cache::class.java) {
            try {
                val list = getDirectory().listFiles()
                if (list != null) {
                    return list.filter {
                        it.name.startsWith(KeyPrefix) && it.isFile
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return listOf()
        }
    }

    companion object {
        private const val KeyPrefix = "f_d_"

        fun packKey(key: String): String {
            return KeyPrefix + key
        }

        fun unpackKey(key: String): String {
            return key.removePrefix(KeyPrefix)
        }
    }
}