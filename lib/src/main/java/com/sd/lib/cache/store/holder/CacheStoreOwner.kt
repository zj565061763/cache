package com.sd.lib.cache.store.holder

import com.sd.lib.cache.store.CacheStore

internal fun interface CacheStoreOwner {
    fun getCacheStore(): CacheStore
}