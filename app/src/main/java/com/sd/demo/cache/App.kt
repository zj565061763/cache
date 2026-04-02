package com.sd.demo.cache

import android.app.Application
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.CacheEntity
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.init
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    CacheConfig.init(this) {
      // 设置异常处理
      setExceptionHandler { error ->
        logMsg { "error:${error}" }
      }
    }

    GlobalScope.launch {
      val keysFlow = MutableStateFlow<List<String>?>(null)
      launch {
        while (true) {
          keysFlow.value = FCacheKtx.get(DefaultModel::class.java).edit { keys() }
          delay(1000)
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