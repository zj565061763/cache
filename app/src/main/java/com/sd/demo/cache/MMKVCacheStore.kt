package com.sd.demo.cache

import android.content.Context
import com.sd.lib.cache.Cache.CacheStore
import com.tencent.mmkv.MMKV

/**
 * 自定义CacheStore
 */
class MMKVCacheStore(context: Context) : CacheStore {
    private val _mmkv: MMKV

    init {
        MMKV.initialize(context)
        _mmkv = MMKV.defaultMMKV()
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return _mmkv.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return _mmkv.decodeBytes(key)
    }

    override fun removeCache(key: String) {
        _mmkv.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return _mmkv.contains(key)
    }
}