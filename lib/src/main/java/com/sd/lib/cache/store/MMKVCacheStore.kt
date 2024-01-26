package com.sd.lib.cache.store

import android.content.Context
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 基于[https://github.com/Tencent/MMKV]实现的仓库
 */
internal class MMKVCacheStore : CacheStore {
    private var _store: MMKV? = null
    private val store: MMKV get() = checkNotNull(_store)

    override fun init(context: Context, directory: File, id: String) {
        initMMKV(context, directory)
        _store?.let { return }
        val safeID = md5(id)
        _store = MMKV.mmkvWithID(safeID)
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        return store.encode(key, value)
    }

    override fun getCache(key: String): ByteArray? {
        return store.decodeBytes(key)
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

    override fun close() {
        _store?.close()
        _store = null
    }

    companion object {
        private val sInit = AtomicBoolean(false)

        fun initMMKV(context: Context, directory: File) {
            if (sInit.compareAndSet(false, true)) {
                MMKV.initialize(context, directory.absolutePath, MMKVLogLevel.LevelNone)
            }
        }
    }
}

private fun md5(input: String): String {
    return MessageDigest.getInstance("MD5")
        .digest(input.toByteArray())
        .let { bytes ->
            bytes.joinToString("") { "%02X".format(it) }
        }
}