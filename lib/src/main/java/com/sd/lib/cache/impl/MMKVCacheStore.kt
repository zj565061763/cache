package com.sd.lib.cache.impl

import android.content.Context
import com.sd.lib.cache.Cache.CacheStore
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File

internal class MMKVCacheStore : CacheStore {
    private lateinit var _mmkv: MMKV

    override fun init(context: Context, directory: File) {
        if (::_mmkv.isInitialized) error("CacheStore is initialized.")
        MMKV.initialize(context, directory.absolutePath, MMKVLogLevel.LevelNone)
        _mmkv = MMKV.defaultMMKV().apply {
            enableAutoKeyExpire(MMKV.ExpireNever)
        }
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return _mmkv.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return _mmkv.decodeBytes(key) ?: kotlin.run {
            if (_mmkv.contains(key)) byteArrayOf() else null
        }
    }

    override fun removeCache(key: String) {
        _mmkv.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return _mmkv.contains(key)
    }
}