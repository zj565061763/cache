package com.sd.lib.cache.store.limit

import java.io.File

/**
 * 限制数量的磁盘缓存
 */
class LimitCountDiskCacheStore(
    /** 最大文件数量，必须大于0 */
    maxCount: Int,
    directory: File,
) : LimitDiskCacheStore(maxCount, directory) {
    override fun sizeOfCache(file: File): Int {
        return 1
    }
}