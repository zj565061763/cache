package com.sd.lib.cache

import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.nio.charset.CharacterCodingException

internal class DefaultObjectConverter : CacheConfig.ObjectConverter {
  private val _moshi = fMoshi.newBuilder()
    .add(String::class.java, StrictStringJsonAdapter.nullSafe())
    .build()

  override fun <T> encode(value: T, clazz: Class<T>): ByteArray {
    return _moshi.adapter(clazz).toJson(value).toByteArray()
  }

  override fun <T> decode(bytes: ByteArray, clazz: Class<T>): T {
    return checkNotNull(
      _moshi.adapter(clazz).fromJson(bytes.decodeToString(throwOnInvalidSequence = true))
    )
  }
}

private object StrictStringJsonAdapter : JsonAdapter<String>() {
  override fun fromJson(reader: JsonReader): String = reader.nextString()

  override fun toJson(writer: JsonWriter, value: String?) {
    checkNotNull(value)
    try {
      value.encodeToByteArray(throwOnInvalidSequence = true)
    } catch (error: CharacterCodingException) {
      libException("Cache value contains invalid UTF-16", error)
    }
    writer.value(value)
  }
}
