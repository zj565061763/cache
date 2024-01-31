package com.sd.demo.cache.impl

import android.content.Context
import android.util.Base64
import com.sd.lib.cache.store.CacheStore
import java.io.File
import java.io.FileNotFoundException

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
        _directory = directory
            .resolve(encodeToFilename(group))
            .resolve(encodeToFilename(id))
        _initFlag = true
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        val file = fileOf(key) ?: return false
        return try {
            file.writeBytes(value)
            true
        } catch (e: FileNotFoundException) {
            if (file.isDirectory) {
                file.deleteRecursively()
                file.writeBytes(value)
                true
            } else {
                throw e
            }
        }
    }

    override fun getCache(key: String): ByteArray? {
        val file = fileOf(key) ?: return null
        return try {
            file.readBytes()
        } catch (e: FileNotFoundException) {
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
        val list = _directory.list()
        if (list.isNullOrEmpty()) return null
        return Array(list.size) { decodeFromFilename(list[it]) }
    }

    override fun close() {
    }

    private fun fileOf(key: String): File? {
        return _directory.let { dir ->
            if (dir.fMakeDirs()) {
                dir.resolve(encodeToFilename(key))
            } else {
                null
            }
        }
    }
}

private fun encodeToFilename(input: String): String {
    val flag = Base64.URL_SAFE or Base64.NO_WRAP
    return Base64.encode(input.toByteArray(), flag).decodeToString()
}

private fun decodeFromFilename(input: String): String {
    val flag = Base64.URL_SAFE or Base64.NO_WRAP
    return Base64.decode(input.toByteArray(), flag).decodeToString()
}

private fun File?.fMakeDirs(): Boolean {
    if (this == null) return false
    if (this.isDirectory) return true
    if (this.isFile) this.delete()
    return this.mkdirs()
}