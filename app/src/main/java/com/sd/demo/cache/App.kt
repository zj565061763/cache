package com.sd.demo.cache

import android.app.Application
import com.sd.lib.cache.CacheConfig
import com.sd.lib.cache.DefaultGroupCache
import com.sd.lib.cache.FCacheKtx
import com.sd.lib.cache.init
import kotlinx.coroutines.GlobalScope
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
      FCacheKtx.get(DefaultModel::class.java)
        .flowOfKeys()
        .collect { keys ->
          logMsg { "App keys:$keys" }
        }
    }
  }
}

@DefaultGroupCache("DefaultModel")
data class DefaultModel(
  val name: String = "tom",
)