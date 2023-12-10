package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File

/**
 * 初始化默认仓库
 */
internal fun initCacheStore(
    context: Context,
    directory: File,
): CacheStore {
    MMKV.initialize(context, directory.absolutePath, MMKVLogLevel.LevelNone)
    return MMKVCacheStore().apply {
        this.init(context, directory)
    }
}

internal interface CacheStore {
    /**
     * 初始化
     */
    fun init(context: Context, directory: File)

    /**
     * 保存缓存
     * @return true-保存成功，false-保存失败
     */
    @Throws(Exception::class)
    fun putCache(key: String, value: ByteArray): Boolean

    /**
     * 获取缓存
     */
    @Throws(Exception::class)
    fun getCache(key: String): ByteArray?

    /**
     * 删除缓存
     */
    @Throws(Exception::class)
    fun removeCache(key: String)

    /**
     * 是否有[key]对应的缓存
     */
    @Throws(Exception::class)
    fun containsCache(key: String): Boolean

    /**
     * 返回所有的Key
     */
    fun keys(): Array<String>?

    /**
     * [key]对应的缓存大小
     */
    fun sizeOf(key: String): Int
}