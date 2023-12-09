package com.sd.lib.cache.handler

import com.sd.lib.cache.CacheInfo

internal class StringHandler(info: CacheInfo) : BaseCacheHandler<String>(info, "string") {
    override fun encode(value: String, clazz: Class<String>?): ByteArray = value.toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): String = String(bytes)
}

internal class IntHandler(info: CacheInfo) : BaseCacheHandler<Int>(info, "int") {
    override fun encode(value: Int, clazz: Class<Int>?): ByteArray = value.toString().toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): Int = String(bytes).toInt()
}

internal class LongHandler(info: CacheInfo) : BaseCacheHandler<Long>(info, "long") {
    override fun encode(value: Long, clazz: Class<Long>?): ByteArray = value.toString().toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): Long = String(bytes).toLong()
}

internal class FloatHandler(info: CacheInfo) : BaseCacheHandler<Float>(info, "float") {
    override fun encode(value: Float, clazz: Class<Float>?): ByteArray = value.toString().toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): Float = String(bytes).toFloat()
}

internal class DoubleHandler(info: CacheInfo) : BaseCacheHandler<Double>(info, "double") {
    override fun encode(value: Double, clazz: Class<Double>?): ByteArray = value.toString().toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): Double = String(bytes).toDouble()
}

internal class BooleanHandler(info: CacheInfo) : BaseCacheHandler<Boolean>(info, "boolean") {
    override fun encode(value: Boolean, clazz: Class<Boolean>?): ByteArray = value.toString().toByteArray()
    override fun decode(bytes: ByteArray, clazz: Class<*>?): Boolean = String(bytes).toBooleanStrict()
}

internal class BytesHandler(info: CacheInfo) : BaseCacheHandler<ByteArray>(info, "bytes") {
    override fun encode(value: ByteArray, clazz: Class<ByteArray>?): ByteArray = value
    override fun decode(bytes: ByteArray, clazz: Class<*>?): ByteArray = bytes
}