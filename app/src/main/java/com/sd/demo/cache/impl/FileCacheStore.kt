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

    override fun putCache(key: String, value: ByteArray): Boolean {
        val file = fileOfKey(key).also { it.fCreateFile() }
        file.writeBytes(value)
        return true
    }

    override fun getCache(key: String): ByteArray? {
        val file = fileOfKey(key)
        return if (file.isFile) file.readBytes() else null
    }

    override fun removeCache(key: String) {
        fileOfKey(key).deleteRecursively()
    }

    override fun containsCache(key: String): Boolean {
        return fileOfKey(key).isFile
    }

    override fun allKeys(): Array<String>? {
        return _directory.list()
    }

    override fun sizeOf(key: String): Int {
        return fileOfKey(key).length().toInt()
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .let { bytes ->
            bytes.joinToString("") { "%02X".format(it) }
        }
}

private fun File?.fCreateFile(): Boolean {
    if (this == null) return false
    if (this.isFile) return true
    if (this.isDirectory) this.deleteRecursively()
    return this.parentFile.fMakeDirs() && this.createNewFile()
}

private fun File?.fMakeDirs(): Boolean {
    if (this == null) return false
    if (this.isDirectory) return true
    if (this.isFile) this.delete()
    return this.mkdirs()
}