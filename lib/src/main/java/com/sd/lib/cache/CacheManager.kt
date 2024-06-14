package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File

internal object CacheManager {
    /** DefaultGroup */
    private const val DEFAULT_GROUP = "com.sd.lib.cache.default.group"

    /** Group对应的[CacheStoreFactory] */
    private val _mapGroupFactory: MutableMap<String, CacheStoreFactory> = mutableMapOf()

    /** ActiveGroup */
    private var _activeGroup = ""

    /**
     * ActiveGroup
     */
    fun getActiveGroup(): String {
        synchronized(CacheLock) {
            return _activeGroup
        }
    }

    /**
     * 设置ActiveGroup
     */
    fun setActiveGroup(group: String) {
        if (group == DEFAULT_GROUP) libError("Require not default group.")
        synchronized(CacheLock) {
            val oldGroup = _activeGroup
            if (oldGroup == group) return

            _activeGroup = group
            _mapGroupFactory.remove(oldGroup)?.close()
        }
    }

    fun cacheStoreOwnerForDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        val cacheStore = getCacheStore(
            group = DEFAULT_GROUP,
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            factory = factory,
        )
        return CacheStoreOwner { cacheStore }
    }

    fun cacheStoreOwnerForActiveGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        return CacheStoreOwner {
            synchronized(CacheLock) {
                val activeGroup = _activeGroup
                if (activeGroup.isEmpty()) {
                    EmptyActiveGroupCacheStore
                } else {
                    getCacheStore(
                        group = activeGroup,
                        id = id,
                        cacheSizePolicy = cacheSizePolicy,
                        factory = factory,
                    )
                }
            }
        }
    }

    private fun getCacheStore(
        group: String,
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        synchronized(CacheLock) {
            return _mapGroupFactory.getOrPut(group) {
                CacheStoreFactory(group)
            }.create(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }
}

internal val CacheLock: Any = CacheManager

private object EmptyActiveGroupCacheStore : CacheStore {
    override fun init(context: Context, directory: File, group: String, id: String) {
        notifyException("Empty active group CacheStore.init()")
    }

    override fun putCache(key: String, value: ByteArray): Boolean {
        notifyException("Empty active group CacheStore.putCache()")
        return false
    }

    override fun getCache(key: String): ByteArray? {
        notifyException("Empty active group CacheStore.getCache()")
        return null
    }

    override fun removeCache(key: String) {
        notifyException("Empty active group CacheStore.removeCache()")
    }

    override fun containsCache(key: String): Boolean {
        notifyException("Empty active group CacheStore.containsCache()")
        return false
    }

    override fun keys(): Array<String>? {
        notifyException("Empty active group CacheStore.keys()")
        return null
    }

    override fun close() {
        notifyException("Empty active group CacheStore.close()")
    }

    private fun notifyException(message: String) {
        libNotifyException(CacheException(message))
    }
}