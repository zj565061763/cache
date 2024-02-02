package com.sd.lib.cache.store

import android.content.Context
import android.util.Base64
import com.sd.lib.cache.libError
import java.io.File
import java.io.FileNotFoundException
import java.security.MessageDigest

internal class FileCacheStore : CacheStore {
    private var _initFlag = false
    private lateinit var _directory: File

    override fun init(
        context: Context,
        directory: File,
        group: String,
        id: String,
    ) {
        if (_initFlag) libError("CacheStore (${group}) (${id}) has already been initialized.")
        _directory = directory.resolve(group.md5()).resolve(id.md5())
        _initFlag = true
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        val file = fileOf(key)
        return try {
            file.writeBytes(value)
            true
        } catch (e: FileNotFoundException) {
            if (_directory.fMakeDirs()) {
                writeFile(file, value)
            } else {
                throw e
            }
        }
    }

    override fun getCache(key: String): ByteArray? {
        val file = fileOf(key)
        return try {
            file.readBytes()
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun removeCache(key: String) {
        fileOf(key).deleteRecursively()
    }

    override fun containsCache(key: String): Boolean {
        return fileOf(key).isFile
    }

    override fun keys(): Array<String>? {
        val list = _directory.list()
        if (list.isNullOrEmpty()) return null
        return Array(list.size) { list[it].decodeKey() }
    }

    override fun close() {
    }

    private fun fileOf(key: String): File {
        return _directory.resolve(key.encodeKey())
    }
}

private fun String.md5(): String {
    val input = this.toByteArray()
    return MessageDigest.getInstance("MD5")
        .digest(input)
        .joinToString("") { "%02X".format(it) }
}

private fun String.encodeKey(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP
    return Base64.encode(input, flag).decodeToString()
}

private fun String.decodeKey(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP
    return Base64.decode(input, flag).decodeToString()
}

private fun File?.fMakeDirs(): Boolean {
    if (this == null) return false
    if (this.isDirectory) return true
    if (this.isFile) this.delete()
    return this.mkdirs()
}

private fun writeFile(file: File, value: ByteArray): Boolean {
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