package com.sd.demo.cache

import com.sd.lib.cache.CacheType

@CacheType("TestModel")
data class TestModel(
    val name: String = "tom",
)