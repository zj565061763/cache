package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File

internal object CacheManager {
    /** DefaultGroup */
    private const val DEFAULT_GROUP = "com.sd.lib.cache.default.group"
    /** DefaultGroup的[CacheStoreFactory] */
    private val _defaultGroupCacheStoreFactory = CacheStoreFactory(DEFAULT_GROUP)

    /** ActiveGroup */
    private var _activeGroup = ""
    /** ActiveGroup的[CacheStoreFactory] */
    private var _activeGroupCacheStoreFactory: CacheStoreFactory? = null

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
        require(group != DEFAULT_GROUP) { "Require not default group" }
        synchronized(CacheLock) {
            _activeGroup = group
        }
    }

    fun cacheStoreOwnerForDefaultGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            _defaultGroupCacheStoreFactory.create(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }

    fun cacheStoreOwnerForActiveGroup(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStoreOwner {
        require(id.isNotEmpty()) { "id is empty" }
        return CacheStoreOwner {
            getActiveGroupCacheStore(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }

    private fun getActiveGroupCacheStore(
        id: String,
        cacheSizePolicy: CacheSizePolicy,
        factory: (CacheConfig) -> CacheStore,
    ): CacheStore {
        val activeGroup = _activeGroup
        if (activeGroup.isEmpty()) {
            _activeGroupCacheStoreFactory?.close()
            _activeGroupCacheStoreFactory = null
            return EmptyActiveGroupCacheStore
        }

        val storeFactory = _activeGroupCacheStoreFactory?.takeIf { it.group == activeGroup }
            ?: CacheStoreFactory(activeGroup).also { newFactory ->
                _activeGroupCacheStoreFactory?.close()
                _activeGroupCacheStoreFactory = newFactory
            }

        return storeFactory.create(
            id = id,
            cacheSizePolicy = cacheSizePolicy,
            factory = factory,
        )
    }
}

internal val CacheLock: Any = FCache

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

    override fun keys(): List<String> {
        notifyException("Empty active group CacheStore.keys()")
        return emptyList()
    }

    override fun close() {
        notifyException("Empty active group CacheStore.close()")
    }

    private fun notifyException(message: String) {
        libNotifyException(CacheException(message))
    }
}