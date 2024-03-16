package com.sd.lib.cache.store.holder

import android.content.Context
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.CacheException
import com.sd.lib.cache.CacheLock
import com.sd.lib.cache.CacheStoreOwner
import com.sd.lib.cache.libError
import com.sd.lib.cache.libNotifyException
import com.sd.lib.cache.store.CacheStore
import java.io.File

internal object CacheStoreOwnerFactory {
    private const val DefaultGroup = "com.sd.lib.cache.default.group"

    private val _holder = GroupCacheStoreHolder()

    /** 当前Group */
    private var _currentGroup = ""

    /**
     * 当前Group
     */
    fun getCurrentGroup(): String {
        synchronized(CacheLock) {
            return _currentGroup
        }
    }

    /**
     * 设置当前Group
     */
    fun setCurrentGroup(group: String) {
        synchronized(CacheLock) {
            val oldGroup = _currentGroup
            if (oldGroup == group) return

            if (group == DefaultGroup) libError("group is default group.")
            _currentGroup = group

            _holder.removeAndClose(oldGroup)
        }
    }

    fun cacheStoreOwnerForDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        val cacheStore = getOrPut(
            group = DefaultGroup,
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            factory = factory,
        )
        return CacheStoreOwner { cacheStore }
    }

    fun cacheStoreOwnerForCurrentGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        return CacheStoreOwner {
            synchronized(CacheLock) {
                val currentGroup = _currentGroup
                if (currentGroup.isEmpty()) {
                    EmptyCurrentGroupCacheStore
                } else {
                    getOrPut(
                        group = currentGroup,
                        id = id,
                        cacheSizePolicy = cacheSizePolicy,
                        factory = factory,
                    )
                }
            }
        }
    }

    private fun getOrPut(
        group: String,
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        synchronized(CacheLock) {
            return _holder.group(group).getOrPut(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }
}

private object EmptyCurrentGroupCacheStore : CacheStore {
    override fun init(context: Context, directory: File, group: String, id: String) {
        notifyException("Empty current group CacheStore.init()")
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        notifyException("Empty current group CacheStore.putCache()")
        return false
    }

    override fun getCache(key: String): ByteArray? {
        notifyException("Empty current group CacheStore.getCache()")
        return null
    }

    override fun removeCache(key: String) {
        notifyException("Empty current group CacheStore.removeCache()")
    }

    override fun containsCache(key: String): Boolean {
        notifyException("Empty current group CacheStore.containsCache()")
        return false
    }

    override fun keys(): Array<String>? {
        notifyException("Empty current group CacheStore.keys()")
        return null
    }

    override fun close() {
        notifyException("Empty current group CacheStore.close()")
    }

    private fun notifyException(message: String) {
        libNotifyException(CacheException(message))
    }
}