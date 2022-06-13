package com.sd.lib.cache.store.limit

import java.io.File

/**
 * 限制文件大小的磁盘缓存
 */
class LimitSizeDiskCacheStore(maxSize: Int, directory: File) : LimitDiskCacheStore(maxSize, directory) {
    override fun sizeOfCache(file: File): Int {
        return file.length().toInt()
    }
}