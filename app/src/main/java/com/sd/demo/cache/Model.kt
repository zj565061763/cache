package com.sd.demo.cache

import com.sd.lib.cache.ActiveGroupCache
import com.sd.lib.cache.DefaultGroupCache

@DefaultGroupCache("DefaultModel")
data class DefaultModel(
    val name: String = "tom",
)

@ActiveGroupCache("ActiveModel")
data class ActiveModel(
    val name: String = "tom",
)