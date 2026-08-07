package com.sd.demo.cache

import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.ReceiveTurbine
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCache
import org.junit.Assert.assertEquals
import java.io.File
import java.security.MessageDigest
import kotlin.time.Duration.Companion.seconds

@CacheEntity("TestDefaultModel")
data class TestDefaultModel(
  val name: String = "tom",
)

/**
 * 测试统一使用[kotlinx.coroutines.runBlocking]而不是`runTest`，等的都是真实时间：
 * [android.os.FileObserver]的事件投递、[kotlinx.coroutines.Dispatchers.IO]上的文件读写，
 * 都不受测试调度器控制，虚拟时间既跳不过它们，还会让`withTimeout`和`delay`失去意义。
 * 所以这里的超时也要显式给足，[app.cash.turbine.test]的默认超时只有1秒。
 */
val TEST_TIMEOUT = 15.seconds

/** 默认缓存组，与[CacheEntity.group]的默认值保持一致 */
private const val DEFAULT_GROUP = "com.sd.lib.cache.group.default"

/** 缓存根目录，与`CacheConfig.directory`的规则保持一致 */
fun cacheRootDirectory(): File {
  val context = InstrumentationRegistry.getInstrumentation().targetContext
  return context.filesDir.resolve("sd.lib.cache")
}

/** [id]对应的缓存目录，与`CacheConfig.newCacheStore()`的规则保持一致 */
fun cacheStoreDirectory(id: String, group: String = DEFAULT_GROUP): File {
  return cacheRootDirectory().resolve(md5(group)).resolve(md5(id))
}

/** 模拟"清除数据"，整个缓存根目录被删除 */
fun deleteCacheDirectory() {
  // 不能断言deleteRecursively()的返回值：删除的过程中，任何一次并发的缓存操作都会把目录重建出来，
  // 此时deleteRecursively()返回false。
  cacheRootDirectory().deleteRecursively()
}

/**
 * 模拟"目录被外部进程删除后，又在同一路径重建"。
 * 重建之后目录本身是存在的，只有[android.os.FileObserver]的DELETE_SELF事件能察觉到监听已失效。
 */
fun recreateCacheStoreDirectory(id: String, group: String = DEFAULT_GROUP) {
  val dir = cacheStoreDirectory(id = id, group = group)
  // 这里要断言删除成功：目录必须真的被删掉，旧的监听才会失效，否则用例的前提就不成立了。
  // 所以调用方必须保证重建期间没有并发的缓存操作，不然并发操作会把目录重建出来导致这里返回false。
  assertEquals(true, dir.deleteRecursively())
  // 不能断言mkdirs()的返回值：目录被删除后，任何一次并发的缓存操作都会把它重建出来，
  // 此时mkdirs()返回false。这里只需要保证目录最终存在。
  dir.mkdirs()
  assertEquals(true, dir.isDirectory)
}

/**
 * 绕过[com.sd.lib.cache.store.CacheStore]直接写缓存文件，模拟其他进程的写入。
 * 写法与`FileCacheStore.putCache()`一致：先写临时文件再重命名，触发MOVED_TO事件。
 */
fun writeCacheFileDirectly(
  id: String,
  key: String,
  json: String,
  group: String = DEFAULT_GROUP,
) {
  val dir = cacheStoreDirectory(id = id, group = group)
  val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
  val filename = Base64.encode(key.toByteArray(), flag).decodeToString()
  val file = dir.resolve("${filename}.cache")
  val tempFile = dir.resolve("${filename}.cache.tmp")
  tempFile.writeBytes(json.toByteArray())
  assertEquals(true, tempFile.renameTo(file))
}

/**
 * 一直读取直到收到[expect]，收不到则由[ReceiveTurbine]的超时机制让测试失败。
 * 缓存目录被删除时会先产生一个null，用这个方法跳过这些中间值。
 */
suspend fun <T> ReceiveTurbine<T>.awaitItemUntil(expect: T): T {
  while (true) {
    val item = awaitItem()
    if (item == expect) return item
  }
}

/** 与`CacheConfig.md5()`的实现保持一致 */
private fun md5(input: String): String {
  val md5Bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
  return buildString {
    for (byte in md5Bytes) {
      val hex = (0xff and byte.toInt()).toString(16)
      if (hex.length == 1) append("0")
      append(hex)
    }
  }
}

fun <T> testCache(
  clazz: Class<T>,
  factory: (key: String) -> T,
) {
  val cache = FCache.get(clazz)

  val key1 = "TestKey1"
  val key2 = "TestKey2"

  val model1 = factory(key1)
  val model2 = factory(key2)

  // test default value
  assertEquals(null, cache.get(key1))
  assertEquals(null, cache.get(key2))

  // test put and get
  assertEquals(true, cache.put(key1, model1))
  assertEquals(true, cache.put(key2, model2))
  assertEquals(model1, cache.get(key1))
  assertEquals(model2, cache.get(key2))

  // test keys
  cache.keys().also {
    assertEquals(2, it.size)
    assertEquals(true, it.contains(key1))
    assertEquals(true, it.contains(key2))
  }

  // test remove and get
  assertEquals(true, cache.remove(key1))
  assertEquals(true, cache.remove(key2))

  assertEquals(null, cache.get(key1))
  assertEquals(null, cache.get(key2))
  assertEquals(0, cache.keys().size)
}