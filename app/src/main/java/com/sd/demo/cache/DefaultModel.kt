package com.sd.demo.cache

import com.sd.lib.cache.ActiveGroupCacheType
import com.sd.lib.cache.DefaultGroupCacheType

@DefaultGroupCacheType("DefaultModel")
data class DefaultModel(
    val name: String = "tom",
)

@ActiveGroupCacheType("ActiveModel")
data class ActiveModel(
    val name: String = "tom",
)