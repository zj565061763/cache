# Changelog

## 3.2.0

### ⚠️ Breaking Changes

- `FCacheKtx.get(clazz)` → `FCache.getKtx(clazz)`
- `CacheKtx.asSingleCacheKtx(...)` → 顶层函数 `singleCacheKtx<T> { default }`；不再支持自定义 `CacheKtx` 接收者和缓存 key，默认单值缓存 key 保持不变
- `coroutineScope` 参数改为 `memoryCache: Boolean`；启用内存缓存后，同一实体类型改为进程级单例，不再由调用方的 `CoroutineScope` 控制生命周期
- `getDefault` 改为创建实例时同步调用：`memoryCache=true` 时仅首次创建该类型实例时调用一次，`false` 时每次创建实例调用一次
- `CacheKtx.edit` / `SingleCacheKtx.update` 的 lambda 不再是 `suspend`
- `Cache.put(key, null)` / `CacheKtx.put(key, null)` 不再支持，改用 `remove(key)`
- `CacheStore.destroy()` 已移除
- `CacheStore.CacheChangeCallback` 新增 `onCleared()` 方法，自定义 `CacheStore` 需实现

### ✨ Improvements

- 新增 `SingleCacheKtx.get()` 扩展方法，通过 `flow().first()` 获取当前缓存值
- FileObserver 掩码收窄为缓存实际需要的事件，减少读文件产生的无效唤醒
- 缓存 key 明确限制为最多 186 个 UTF-8 字节，超限时向 `ExceptionHandler` 提供可读错误

### 🐛 Bug Fixes

- 目录被整体移走（MOVE_SELF）时，`flowOf` 订阅者不会收到通知
- 目录被删除或外部重建后，FileObserver 监听失效，后续 `put/get/remove/keys` 无法可靠恢复 Flow 更新
- 单个缓存文件被移出目录（MOVED_FROM）时未按删除事件通知订阅者
- `flowOf` 初始值读取与回调注册之间存在竞态，可能丢失更新
- `edit { }` 在特定锁级别下存在死锁隐患
- 内存缓存连续快速 `update` 时，Flow 可能短暂发射旧值
- `memoryCache=true` 冷启动时（热流尚未初始化），`get()` / `flow().first()` 可能挂起或返回默认缓存而非磁盘真实值
- `memoryCache=true` 时，缓存目录被清除后热流无法可靠回退到默认值并恢复后续更新
- `keys()` 可能包含无法解码为合法 UTF-8 的乱码 key
- 多个进程并发写入同一 key 时共用固定临时文件，可能导致写入失败或缓存文件损坏
- 一个进程初始化缓存时可能清理其他进程正在写入的临时文件
- 在 `CacheConfig.init()` 前创建单值内存缓存可能导致热流永久等待

### Migration

```kotlin
// FCacheKtx → FCache.getKtx
- FCacheKtx.get(MyModel::class.java)
+ FCache.getKtx(MyModel::class.java)

// asSingleCacheKtx → singleCacheKtx
- cacheKtx.asSingleCacheKtx(coroutineScope = scope) { MyModel() }
+ singleCacheKtx<MyModel>(memoryCache = true) { MyModel() }

- cacheKtx.asSingleCacheKtx { MyModel() }
+ singleCacheKtx<MyModel> { MyModel() }

// getDefault 现在在调用 singleCacheKtx 时同步执行，不再切换到 Dispatchers.IO

// put(null) → remove
- cache.put(key, null)
+ cache.remove(key)

// 自定义 CacheChangeCallback 需实现 onCleared()
+ override fun onCleared() { ... }
```

如果旧代码通过 `asSingleCacheKtx(key = "...")` 使用了自定义 key，新 API 不会自动读取该位置；升级前应通过 `FCache.getKtx(clazz)` 读取旧 key，将数据写入新的 `SingleCacheKtx` 后再删除旧值。
