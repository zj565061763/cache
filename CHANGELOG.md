# Changelog

## 3.2.0

### ⚠️ Breaking Changes

- `FCacheKtx.get(clazz)` → `FCache.getKtx(clazz)`
- `CacheKtx.asSingleCacheKtx(...)` → 顶层函数 `singleCacheKtx<T> { default }`，`coroutineScope` 参数改为 `memoryCache: Boolean`
- `Cache.put` / `SingleCacheKtx.update` 的 lambda 不再是 `suspend`
- `Cache.put(key, null)` 不再支持，改用 `remove(key)`
- `CacheStore.destroy()` 已移除
- `CacheStore.CacheChangeCallback` 新增 `onCleared()` 方法，自定义 `CacheStore` 需实现

### ✨ Improvements

- 新增 `SingleCacheKtx.get()` 扩展方法，通过 `flow().first()` 获取当前缓存值
- `CacheKtx.eventFlowOf(key)` 作为公开 API 暴露
- FileObserver 掩码收窄，减少无效唤醒
- 超长 key 给出明确的错误信息

### 🐛 Bug Fixes

- 目录被整体移走（MOVE_SELF）时，`flowOf` 订阅者不会收到通知
- 目录被删除后，FileObserver 监听失效，Flow 不再更新
- `flowOf` 初始值读取与回调注册之间存在竞态，可能丢失更新
- `edit { }` 在特定锁级别下存在死锁隐患
- 内存缓存连续快速 `update` 时，Flow 可能短暂发射旧值
- `keys()` 可能包含无法解码为合法 UTF-8 的乱码 key

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

// put(null) → remove
- cache.put(key, null)
+ cache.remove(key)

// 自定义 CacheChangeCallback 需实现 onCleared()
+ override fun onCleared() { ... }
```