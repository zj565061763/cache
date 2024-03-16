package com.sd.lib.cache

import android.content.Context
import com.sd.lib.cache.store.CacheStore
import java.io.File

internal object CacheManager {
    /** 默认的Group */
    private const val DEFAULT_GROUP = "com.sd.lib.cache.default.group"

    /** Group对应的[CacheStoreFactory] */
    private val _mapGroupFactory: MutableMap<String, CacheStoreFactory> = hashMapOf()

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
        if (group == DEFAULT_GROUP) libError("Require not default group.")
        synchronized(CacheLock) {
            val oldGroup = _currentGroup
            if (oldGroup == group) return

            _currentGroup = group
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
                    getCacheStore(
                        group = currentGroup,
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
            }.getOrPut(
                id = id,
                cacheSizePolicy = cacheSizePolicy,
                factory = factory,
            )
        }
    }
}

internal val CacheLock: Any = CacheManager

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