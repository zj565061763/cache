package com.sd.lib.cache.store

import java.io.File
import java.io.FileNotFoundException

internal class FileCacheStore : DirectoryCacheStore() {
    override fun putCacheImpl(file: File, value: ByteArray) {
        try {
            file.writeBytes(value)
        } catch (e: FileNotFoundException) {
            if (directory.fMakeDirs()) {
                file.writeBytes(value)
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

private fun File.fMakeDirs(): Boolean {
    if (isDirectory) return true
    if (isFile) delete()
    return mkdirs()
}