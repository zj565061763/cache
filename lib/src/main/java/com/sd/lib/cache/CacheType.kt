package com.sd.lib.cache

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DefaultGroupCache(
    val id: String,
    val limitCount: Int = 0,
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ActiveGroupCache(
    val id: String,
    val limitCount: Int = 0,
)