package com.sd.lib.cache.store.lru

import java.io.File

/**
 * 限制大小的Lru算法磁盘缓存
 */
class SizeDiskLruCacheStore(
    maxSize: Int,
    directory: File,
) : DiskLruCacheStore(maxSize, directory) {
    override fun onLruCacheSizeOfEntry(file: File): Int {
        return try {
            file.length().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}