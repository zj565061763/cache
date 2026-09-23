package com.sd.lib.cache

import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.nio.charset.CharacterCodingException

internal class DefaultObjectConverter : CacheConfig.ObjectConverter {
  private val _moshi = fMoshi.newBuilder()
    .add(String::class.java, StrictStringJsonAdapter.nullSafe())
    .add(Char::class.javaPrimitiveType!!, StrictCharJsonAdapter)
    .add(Char::class.javaObjectType, StrictCharJsonAdapter.nullSafe())
    .build()

  override fun <T> encode(value: T, clazz: Class<T>): ByteArray {
    return _moshi.adapter(clazz).toJson(value).toByteArray()
  }

  override fun <T> decode(bytes: ByteArray, clazz: Class<T>): T {
    return checkNotNull(
      _moshi.adapter(clazz).fromJson(bytes.decodeToString())
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

private object StrictCharJsonAdapter : JsonAdapter<Char>() {
  override fun fromJson(reader: JsonReader): Char {
    val value = reader.nextString()
    if (value.length != 1) {
      throw JsonDataException("Expected a char but was \"$value\" at path ${reader.path}")
    }
    return value[0]
  }

  override fun toJson(writer: JsonWriter, value: Char?) {
    checkNotNull(value)
    if (Character.isSurrogate(value)) {
      libException("Cache value contains invalid UTF-16", CharacterCodingException())
    }
    writer.value(value.toString())
  }
}
