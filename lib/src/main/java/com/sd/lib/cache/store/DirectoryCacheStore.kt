package com.sd.lib.cache.store

import android.content.Context
import android.util.Base64
import com.sd.lib.cache.libError
import java.io.File
import java.security.MessageDigest

abstract class DirectoryCacheStore : CacheStore {
    private var _initFlag = false
    private lateinit var _directory: File

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
        val encodeKey = key.encodeKey()
        return putCacheImpl(encodeKey, value, encodeKey.keyFile())
    }

    final override fun getCache(key: String): ByteArray? {
        val encodeKey = key.encodeKey()
        return getCacheImpl(encodeKey, encodeKey.keyFile())
    }

    final override fun removeCache(key: String) {
        val encodeKey = key.encodeKey()
        removeCacheImpl(encodeKey, encodeKey.keyFile())
    }

    final override fun containsCache(key: String): Boolean {
        val encodeKey = key.encodeKey()
        return containsCacheImpl(encodeKey, encodeKey.keyFile())
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

    private fun String.keyFile(): File {
        return _directory.resolve(this)
    }

    protected open fun initImpl(context: Context, directory: File) = Unit

    protected abstract fun putCacheImpl(key: String, value: ByteArray, file: File): Boolean

    protected abstract fun getCacheImpl(key: String, file: File): ByteArray?

    protected open fun removeCacheImpl(key: String, file: File) = file.deleteRecursively()

    protected open fun containsCacheImpl(key: String, file: File): Boolean = file.isFile
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
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) append("0")
            append(hex)
        }
    }
}