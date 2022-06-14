package com.sd.lib.cache.store.lru

import java.io.File

class CountDiskLruCacheStore(
    maxSize: Int,
    directory: File,
) : DiskLruCacheStore(maxSize, directory) {
    override fun onLruCacheSizeOfEntry(file: File): Int {
        return 1
    }
}