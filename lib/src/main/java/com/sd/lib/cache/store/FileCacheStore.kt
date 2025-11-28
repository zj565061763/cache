package com.sd.lib.cache.store

import java.io.File
import java.io.FileNotFoundException

internal class FileCacheStore : DirectoryCacheStore() {
  override fun putCacheImpl(file: File, value: ByteArray) {
    try {
      file.writeBytes(value)
    } catch (e: FileNotFoundException) {
      if (checkDirectoryExist()) {
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