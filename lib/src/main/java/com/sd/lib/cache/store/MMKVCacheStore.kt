package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import java.io.File

internal class MMKVCacheStore : CacheStore {
    private lateinit var _mmkv: MMKV

    override fun init(context: Context, directory: File) {
        if (::_mmkv.isInitialized) return
        _mmkv = MMKV.mmkvWithID(directory.absolutePath)
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


    override fun keys(): Array<String>? {
        return _mmkv.allKeys()
    }

    override fun sizeOf(key: String): Int {
        return _mmkv.decodeBytes(key)?.size ?: 0
    }
}