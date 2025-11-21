package com.sd.lib.cache

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.io.File

/** 默认缓存目录 */
internal fun Context.defaultCacheDir(): File {
  return filesDir.resolve("sd.lib.cache")
}

/** 当前进程名称 */
internal fun Context.currentProcess(): String? {
  // TODO 判断进程，按进程目录存储
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    Application.getProcessName()
  } else {
    val pid = Process.myPid()
    (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
      ?.runningAppProcesses
      ?.firstOrNull { it.pid == pid }
      ?.processName
  }
}