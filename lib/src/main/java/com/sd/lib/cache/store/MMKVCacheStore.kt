package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import java.io.File

/**
 * 基于腾讯MMKV实现的仓库
 */
internal class MMKVCacheStore : CacheStore, AutoCloseable {
    private var _mmkv: MMKV? = null
    private val mmkv: MMKV get() = checkNotNull(_mmkv)

    override fun init(context: Context, directory: File) {
        _mmkv?.let { return }
        _mmkv = MMKV.mmkvWithID(directory.absolutePath)
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return mmkv.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return mmkv.decodeBytes(key) ?: kotlin.run {
            if (mmkv.contains(key)) byteArrayOf() else null
        }
    }

    override fun removeCache(key: String) {
        mmkv.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return mmkv.contains(key)
    }


    override fun keys(): Array<String>? {
        return mmkv.allKeys()
    }

    override fun sizeOf(key: String): Int {
        return mmkv.getValueActualSize(key)
    }

    override fun close() {
        try {
            _mmkv?.close()
        } finally {
            _mmkv = null
        }
    }
}