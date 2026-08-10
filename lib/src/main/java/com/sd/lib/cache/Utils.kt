package com.sd.lib.cache

import java.security.MessageDigest

internal fun md5(input: String): String {
  val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
  return buildString {
    for (byte in bytes) {
      val hex = (0xff and byte.toInt()).toString(16)
      if (hex.length == 1) append("0")
      append(hex)
    }
  }
}