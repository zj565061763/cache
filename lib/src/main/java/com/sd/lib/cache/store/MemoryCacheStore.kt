package com.sd.lib.cache.store

import android.content.Context
import java.io.File

/**
 * 保存在内存中的仓库
 */
class MemoryCacheStore : CacheStore {
    private val _holder: MutableMap<String, ByteArray> = hashMapOf()

    override fun init(context: Context, directory: File, id: String) {
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        _holder[key] = value
        return true
    }

    override fun getCache(key: String): ByteArray? {
        return _holder[key]
    }

    override fun removeCache(key: String) {
        _holder.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return _holder.containsKey(key)
    }

    override fun keys(): Array<String> {
        return _holder.keys.toTypedArray()
    }

    override fun close() {
    }
}