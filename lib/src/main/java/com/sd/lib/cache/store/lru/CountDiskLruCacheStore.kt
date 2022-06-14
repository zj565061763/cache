package com.sd.lib.cache.store.lru

import java.io.File

/**
 * 限制数量的Lru算法磁盘缓存
 */
class CountDiskLruCacheStore(
    maxCount: Int,
    directory: File,
) : DiskLruCacheStore(maxCount, directory) {
    override fun onLruCacheSizeOfEntry(file: File): Int {
        return 1
    }
}