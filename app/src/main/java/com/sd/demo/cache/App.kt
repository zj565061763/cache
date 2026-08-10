package com.sd.demo.cache

import android.app.Application
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCache
import com.sd.lib.cache.init
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

class App : Application() {
  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate() {
    super.onCreate()
    CacheConfig.init(this) {
      // 设置异常处理
      setExceptionHandler { error ->
        logMsg { "error:${error}" }
        CacheErrors.add(error)
      }
    }

    GlobalScope.launch {
      val keysFlow = MutableStateFlow<List<String>?>(null)
      launch {
        while (true) {
          keysFlow.value = FCache.get(DefaultModel::class.java).keys()
          delay(1.seconds)
        }
      }
      launch {
        keysFlow.filterNotNull().distinctUntilChanged().collect { keys ->
          logMsg { "App keys:$keys" }
        }
      }
    }
  }
}

@CacheEntity("DefaultModel")
data class DefaultModel(
  val name: String = "default",
)

/**
 * 测试用：记录[CacheConfig]的异常处理器收到的异常。
 * [CacheConfig.init]只能调用一次、且发生在[Application.onCreate]，测试没办法替换异常处理器，
 * 所以只能在这里开一个口子，让测试能断言异常的内容。
 */
object CacheErrors {
  private val _errors = Collections.synchronizedList(mutableListOf<Throwable>())

  fun add(error: Throwable) {
    _errors.add(error)
  }

  fun clear() {
    _errors.clear()
  }

  fun list(): List<Throwable> = _errors.toList()
}