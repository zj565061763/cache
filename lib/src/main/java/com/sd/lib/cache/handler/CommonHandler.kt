package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheInfo

internal class StringHandler(info: CacheInfo) : BaseCacheHandler<String>(info, "string") {
    override fun encodeToByteImpl(value: String, clazz: Class<*>?): ByteArray = value.toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): String = String(bytes)
}

internal class IntHandler(info: CacheInfo) : BaseCacheHandler<Int>(info, "int") {
    override fun encodeToByteImpl(value: Int, clazz: Class<*>?): ByteArray = value.toString().toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Int = String(bytes).toInt()
}

internal class LongHandler(info: CacheInfo) : BaseCacheHandler<Long>(info, "long") {
    override fun encodeToByteImpl(value: Long, clazz: Class<*>?): ByteArray = value.toString().toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Long = String(bytes).toLong()
}

internal class FloatHandler(info: CacheInfo) : BaseCacheHandler<Float>(info, "float") {
    override fun encodeToByteImpl(value: Float, clazz: Class<*>?): ByteArray = value.toString().toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Float = String(bytes).toFloat()
}

internal class DoubleHandler(info: CacheInfo) : BaseCacheHandler<Double>(info, "double") {
    override fun encodeToByteImpl(value: Double, clazz: Class<*>?): ByteArray = value.toString().toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Double = String(bytes).toDouble()
}

internal class BooleanHandler(info: CacheInfo) : BaseCacheHandler<Boolean>(info, "boolean") {
    override fun encodeToByteImpl(value: Boolean, clazz: Class<*>?): ByteArray = value.toString().toByteArray()
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): Boolean = String(bytes).toBooleanStrict()
}

internal class BytesHandler(info: CacheInfo) : BaseCacheHandler<ByteArray>(info, "bytes") {
    override fun encodeToByteImpl(value: ByteArray, clazz: Class<*>?): ByteArray = value
    override fun decodeFromByteImpl(bytes: ByteArray, clazz: Class<*>?): ByteArray = bytes
}