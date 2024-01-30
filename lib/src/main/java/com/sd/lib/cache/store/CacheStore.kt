package com.sd.lib.cache.store

import android.content.Context
import java.io.File

/**
 * 缓存仓库
 */
interface CacheStore {
    /**
     * 初始化
     */
    fun init(context: Context, directory: File, id: String)

    /**
     * 保存缓存
     * @return true-保存成功，false-保存失败
     */
    @Throws(Throwable::class)
    fun putCache(key: String, value: ByteArray): Boolean

    /**
     * 获取缓存
     */
    @Throws(Throwable::class)
    fun getCache(key: String): ByteArray?

    /**
     * 删除缓存
     */
    @Throws(Throwable::class)
    fun removeCache(key: String)

    /**
     * 是否有[key]对应的缓存
     */
    @Throws(Throwable::class)
    fun containsCache(key: String): Boolean

    /**
     * 返回所有的Key
     */
    fun keys(): Array<String>?

    /**
     * 关闭
     */
    @Throws(Throwable::class)
    fun close()
}