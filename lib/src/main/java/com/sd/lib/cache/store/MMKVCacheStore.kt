package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import java.io.File
import java.security.MessageDigest

/**
 * 基于[https://github.com/Tencent/MMKV]实现的仓库
 */
internal class MMKVCacheStore : CacheStore {
    private var _store: MMKV? = null
    private val store: MMKV get() = checkNotNull(_store)

    override fun init(context: Context, directory: File, id: String) {
        _store?.let { return }
        _store = MMKV.mmkvWithID(md5(id))
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return store.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return store.decodeBytes(key) ?: kotlin.run {
            if (store.contains(key)) byteArrayOf() else null
        }
    }

    override fun removeCache(key: String) {
        store.remove(key)
    }

    override fun containsCache(key: String): Boolean {
        return store.contains(key)
    }

    override fun keys(): Array<String>? {
        return store.allKeys()
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .let { bytes ->
            bytes.joinToString("") { "%02X".format(it) }
        }
}