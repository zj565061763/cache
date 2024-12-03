package com.sd.lib.cache

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCacheType(
    val id: String,
    val limitCount: Int = 0,
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ActiveGroupCacheType(
    val id: String,
    val limitCount: Int = 0,
)