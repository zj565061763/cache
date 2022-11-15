# About

封装了一层存储的api

* 支持基本数据类型和对象存储
* 支持自定义底层的存储方案，例如用腾讯的[MMKV](https://github.com/Tencent/MMKV)实现

# Gradle

[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

# 初始化

```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        CacheConfig.init(new CacheConfig.Builder()
                /**
                 * 使用腾讯MMKV自定义的CacheStore，如果不设置，默认使用内部存储目录"/data/包名/files/f_disk_cache"
                 */
                .setCacheStore(new MMKVCacheStore(this))
                /**
                 * 设置对象转换器
                 */
                .setObjectConverter(new GsonObjectConverter())
                /**
                 * 设置异常监听
                 */
                .setExceptionHandler(new GlobalExceptionHandler())
                .build(this)
        );
    }
}
```

# 简单demo

```java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY = "key";
    private static final TestModel TEST_MODEL = new TestModel();

    private Cache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        putData();
        getData();
    }

    private Cache getCache() {
        if (mCache == null) {
            /**
             * 使用本地磁盘缓存
             * <p>
             * 默认使用内部存储目录"/data/包名/files/f_disk_cache"，可以在初始化的时候设置{@link CacheConfig.Builder#setCacheStore(Cache.CacheStore)}
             */
            mCache = FCache.disk();
        }
        return mCache;
    }

    private void putData() {
        getCache().cacheInteger().put(KEY, 1);
        getCache().cacheLong().put(KEY, 22L);
        getCache().cacheFloat().put(KEY, 333.333F);
        getCache().cacheDouble().put(KEY, 4444.4444D);
        getCache().cacheBoolean().put(KEY, true);
        getCache().cacheString().put(KEY, "hello String");
        getCache().cacheObject().put(TEST_MODEL);
        getCache().cacheMultiObject(TestModel.class).put(KEY, TEST_MODEL);
    }

    private void getData() {
        Log.i(TAG, "cacheInteger:" + getCache().cacheInteger().get(KEY, 0));
        Log.i(TAG, "cacheLong:" + getCache().cacheLong().get(KEY, 0L));
        Log.i(TAG, "cacheFloat:" + getCache().cacheFloat().get(KEY, 0F));
        Log.i(TAG, "cacheDouble:" + getCache().cacheDouble().get(KEY, 0D));
        Log.i(TAG, "cacheBoolean:" + getCache().cacheBoolean().get(KEY, false));
        Log.i(TAG, "cacheString:" + getCache().cacheString().get(KEY, null));
        Log.i(TAG, "cacheObject:" + getCache().cacheObject().get(TestModel.class));
        Log.i(TAG, "cacheMultiObject:" + getCache().cacheMultiObject(TestModel.class).get(KEY));
    }
}
```

# 对象转换器

```java
public class GsonObjectConverter implements Cache.ObjectConverter {
    private final Gson gson = new Gson();

    @NonNull
    @Override
    public byte[] objectToByte(@NonNull Object value) throws Exception {
        return gson.toJson(value).getBytes();
    }

    @NonNull
    @Override
    public <T> T byteToObject(@NonNull byte[] bytes, @NonNull Class<T> clazz) throws Exception {
        return gson.fromJson(new String(bytes), clazz);
    }
}
```

# 自定义CacheStore

```java
/**
 * 自定义CacheStore
 */
public class MMKVCacheStore implements Cache.CacheStore {
    private final MMKV _mmkv;

    public MMKVCacheStore(Context context) {
        MMKV.initialize(context);
        _mmkv = MMKV.defaultMMKV();
    }

    @Override
    public boolean putCache(@NonNull String key, @NonNull byte[] value) {
        return _mmkv.encode(key, value);
    }

    @Override
    public byte[] getCache(@NonNull String key) {
        return _mmkv.decodeBytes(key);
    }

    @Override
    public boolean removeCache(@NonNull String key) {
        _mmkv.remove(key);
        return true;
    }

    @Override
    public boolean containsCache(@NonNull String key) {
        return _mmkv.contains(key);
    }
}
```
