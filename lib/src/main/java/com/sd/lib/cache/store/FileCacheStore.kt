package com.sd.lib.cache.store

import java.io.File
import java.io.FileNotFoundException

internal class FileCacheStore : DirectoryCacheStore() {

    override fun putCacheImpl(file: File, value: ByteArray): Boolean {
        return try {
            file.writeBytes(value)
            true
        } catch (e: FileNotFoundException) {
            if (directory.fMakeDirs()) {
                writeFile(file, value)
            } else {
                throw e
            }
        }
    }

    override fun getCacheImpl(file: File): ByteArray? {
        return try {
            file.readBytes()
        } catch (e: FileNotFoundException) {
            null
        }
    }
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