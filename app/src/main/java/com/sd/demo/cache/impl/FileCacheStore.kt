package com.sd.demo.cache.impl

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File
import java.security.MessageDigest

class FileCacheStore : CacheStore {
    private lateinit var _directory: File

    override fun init(context: Context, directory: File, id: String) {
        if (::_directory.isInitialized) return
        _directory = directory
    }

    private fun fileOfKey(key: String): File {
        return _directory.resolve(md5(key))
    }

    private fun fileResource(key: String): FileResource {
        val file = fileOfKey(key)
        return FileResource.create(file)
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        fileResource(key).write(value)
        return true
    }

    override fun getCache(key: String): ByteArray? {
        return fileResource(key).read()
    }

    override fun removeCache(key: String) {
        fileResource(key).delete()
    }

    override fun containsCache(key: String): Boolean {
        return fileOfKey(key).isFile
    }

    override fun allKeys(): Array<String>? {
        return _directory.list()
    }

    override fun sizeOf(key: String): Int {
        return fileResource(key).size().toInt()
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .let { bytes ->
            bytes.joinToString("") { "%02X".format(it) }
        }
}