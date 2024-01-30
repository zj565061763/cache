package com.sd.demo.cache.impl

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File
import java.security.MessageDigest

class FileCacheStore : CacheStore {
    private var _initFlag = false

    private lateinit var _directory: File
    private lateinit var _group: String
    private lateinit var _id: String

    private lateinit var _groupDir: File

    override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        if (_initFlag) error("CacheStore has already been initialized.")

        _directory = directory
        _group = group
        _id = id

        _groupDir = directory.resolve(md5(group)).resolve(md5(id))
        _initFlag = true
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        val file = fileOf(key) ?: return false
        file.writeBytes(value)
        return true
    }

    override fun getCache(key: String): ByteArray? {
        val file = fileOf(key) ?: return null
        return file.readBytes()
    }

    override fun removeCache(key: String) {
        fileOf(key)?.deleteRecursively()
    }

    override fun containsCache(key: String): Boolean {
        val file = fileOf(key) ?: return false
        return file.isFile
    }

    override fun keys(): Array<String>? {
        return _groupDir.list()
    }

    override fun close() {
    }

    private fun fileOf(key: String): File? {
        val dir = _groupDir
        if (!dir.fMakeDirs()) return null
        return dir.resolve(md5(key))
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