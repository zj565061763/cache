package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

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

    companion object {
        private val sInit = AtomicBoolean(false)

        private lateinit var sDefaultDirectory: File
        private lateinit var sDefaultStore: CacheStore

        /**
         * 初始化
         */
        fun init(context: Context, directory: File): CacheStore {
            if (sInit.compareAndSet(false, true)) {
                MMKV.initialize(context, directory.absolutePath, MMKVLogLevel.LevelNone)
                sDefaultDirectory = directory
                sDefaultStore = MMKVCacheStore().apply {
                    this.init(context, directory)
                }
            }
            return sDefaultStore
        }
    }
}