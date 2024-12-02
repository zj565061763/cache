package com.sd.lib.cache.store

import android.content.Context
import android.util.Base64
import com.sd.lib.cache.libError
import java.io.File
import java.security.MessageDigest

abstract class DirectoryCacheStore : CacheStore {
    private var _initFlag = false
    private lateinit var _directory: File

    protected val directory: File
        get() = _directory

    final override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        if (_initFlag) libError("CacheStore (${group}) (${id}) has already been initialized.")
        _directory = directory.resolve(fMd5(group)).resolve(fMd5(id))
        _initFlag = true
        initImpl(context, _directory)
    }

    final override fun putCache(key: String, value: ByteArray): Boolean {
        return putCacheImpl(fileOf(key), value)
    }

    final override fun getCache(key: String): ByteArray? {
        return getCacheImpl(fileOf(key))
    }

    final override fun removeCache(key: String) {
        removeCacheImpl(fileOf(key))
    }

    final override fun containsCache(key: String): Boolean {
        return containsCacheImpl(fileOf(key))
    }

    final override fun keys(): Array<String>? {
        val list = _directory.list()
        if (list.isNullOrEmpty()) return null
        return list.asSequence()
            .filterNotNull()
            .map { filename ->
                try {
                    filename.decodeKey()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    _directory.resolve(filename).deleteRecursively()
                    null
                }
            }
            .filterNotNull()
            .toList()
            .toTypedArray()
    }

    override fun close() = Unit

    private fun fileOf(key: String): File {
        return _directory.resolve(key.encodeKey())
    }

    protected open fun initImpl(context: Context, directory: File) = Unit
    protected abstract fun putCacheImpl(file: File, value: ByteArray): Boolean
    protected abstract fun getCacheImpl(file: File): ByteArray?
    protected open fun removeCacheImpl(file: File) = file.deleteRecursively()
    protected open fun containsCacheImpl(file: File): Boolean = file.isFile
}

private fun String.encodeKey(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    return Base64.encode(input, flag).decodeToString()
}

@Throws(IllegalArgumentException::class)
private fun String.decodeKey(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    return Base64.decode(input, flag).decodeToString()
}

private fun fMd5(input: String): String {
    val md5Bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
    return buildString {
        for (byte in md5Bytes) {
            val hex = (0xff and byte.toInt()).toString(16)
            if (hex.length == 1) append("0")
            append(hex)
        }
    }
}