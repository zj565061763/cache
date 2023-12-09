package com.sd.demo.cache

import android.content.Context
import com.sd.lib.cache.Cache.CacheStore
import com.tencent.mmkv.MMKV

/**
 * 自定义CacheStore
 *
 * MMKV有个bug，如果存的是空byte[]，取的时候会返回null，正常返回应该也是个空byte[]
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