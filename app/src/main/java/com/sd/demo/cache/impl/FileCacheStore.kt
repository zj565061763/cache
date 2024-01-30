package com.sd.demo.cache.impl

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File
import java.io.IOException
import java.security.MessageDigest

class FileCacheStore : CacheStore {
    private var _initFlag = false
    private lateinit var _directory: File

    override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        if (_initFlag) error("CacheStore has already been initialized.")
        _directory = directory.resolve(md5(group)).resolve(md5(id))
        _initFlag = true
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        val file = fileOf(key) ?: return false
        file.writeBytes(value)
        return true
    }

    override fun getCache(key: String): ByteArray? {
        val file = fileOf(key) ?: return null
        return try {
            file.readBytes()
        } catch (e: IOException) {
            null
        }
    }

    override fun removeCache(key: String) {
        fileOf(key)?.deleteRecursively()
    }

    override fun containsCache(key: String): Boolean {
        val file = fileOf(key) ?: return false
        return file.isFile
    }

    override fun keys(): Array<String>? {
        return _directory.list()
    }

    override fun close() {
    }

    private fun fileOf(key: String): File? {
        return _directory.let { dir ->
            if (dir.fMakeDirs()) {
                dir.resolve(md5(key))
            } else {
                null
            }
        }
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .joinToString("") { "%02X".format(it) }
}

private fun File?.fMakeDirs(): Boolean {
    if (this == null) return false
    if (this.isDirectory) return true
    if (this.isFile) this.delete()
    return this.mkdirs()
}