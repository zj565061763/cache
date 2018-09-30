# About
封装了一层存储的api，支持加解密，支持对象存储，支持内存缓存，支持自定义底层不同的存储方案，比如：
* 用java原生api把缓存写入文件
* 基于腾讯[MMKV](https://github.com/Tencent/MMKV)的实现

# Gradle
[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

# 初始化
```java
// 初始化
FCache.init(new CacheConfig.Builder()
        // 如果需要加解密，设置全局加解密转换器
        .setEncryptConverter(new GlobalEncryptConverter())
        // 设置全局Gson对象转换器
        .setObjectConverter(new GsonObjectConverter())
        // 设置全局异常监听
        .setExceptionHandler(new GlobalExceptionHandler())
        .build(this)
);
```

# 简单demo

```java
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String KEY = "key";
    private static final TestModel TEST_MODEL = new TestModel();

    private Cache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        putData();
        printData();
    }

    private Cache getCache()
    {
        if (mCache == null)
        {
            /**
             * 自定义Cache，底层用腾讯微信的MMKV库实现存储
             */
            mCache = new MMKVCache();

            /**
             * 使用指定的目录
             */
            mCache = FDisk.openDir(Environment.getExternalStorageDirectory());

            /**
             * 优先使用外部存储，如果外部存储不存在，则使用内部存储
             *
             * 外部存储目录"Android/data/包名/files/disk_file"
             */
            mCache = FDisk.openExternal();

            /**
             * 使用内部存储
             *
             * 内部存储目录"/data/包名/files/disk_file"
             */
            mCache = FDisk.open();
        }
        return mCache;
    }

    private void putData()
    {
        getCache().cacheInteger().put(KEY, 1);
        getCache().cacheLong().put(KEY, 22L);
        getCache().cacheFloat().put(KEY, 333.333F);
        getCache().cacheDouble().put(KEY, 4444.4444D);
        getCache().cacheBoolean().put(KEY, true);
        getCache().cacheString().put(KEY, "hello String");
        getCache().cacheObject().put(TEST_MODEL);
    }

    private void printData()
    {
        Log.i(TAG, "getInt:" + getCache().cacheInteger().get(KEY));
        Log.i(TAG, "getLong:" + getCache().cacheLong().get(KEY));
        Log.i(TAG, "getFloat:" + getCache().cacheFloat().get(KEY));
        Log.i(TAG, "getDouble:" + getCache().cacheDouble().get(KEY));
        Log.i(TAG, "getBoolean:" + getCache().cacheBoolean().get(KEY));
        Log.i(TAG, "getString:" + getCache().cacheString().get(KEY));
        Log.i(TAG, "getObject:" + getCache().cacheObject().get(TestModel.class));
    }
}
```

# 对象转换器
```java
public class GsonObjectConverter implements Cache.ObjectConverter
{
    private static final Gson GSON = new Gson();

    @Override
    public byte[] objectToByte(Object object)
    {
        // 对象转byte
        return GSON.toJson(object).getBytes();
    }

    @Override
    public <T> T byteToObject(byte[] bytes, Class<T> clazz)
    {
        // byte转对象
        return GSON.fromJson(new String(bytes), clazz);
    }
}
```

# 加解密转换器
```java
public class GlobalEncryptConverter implements Cache.EncryptConverter
{
    @Override
    public byte[] encrypt(byte[] bytes)
    {
        // 加密
        return bytes;
    }

    @Override
    public byte[] decrypt(byte[] bytes)
    {
        // 解密
        return bytes;
    }
}
```

# 自定义Cache

```java
/**
 * 自定义Cache，底层用腾讯微信的MMKV库实现存储
 */
public class MMKVCache extends FCache
{
    private final MMKV mMMKV = MMKV.defaultMMKV();

    @Override
    public CacheStore getCacheStore()
    {
        return mCacheStore;
    }

    private final CacheStore mCacheStore = new CacheStore()
    {
        @Override
        public boolean putCache(String key, byte[] value, CacheInfo info)
        {
            return mMMKV.encode(key, value);
        }

        @Override
        public byte[] getCache(String key, Class clazz, CacheInfo info)
        {
            return mMMKV.decodeBytes(key);
        }

        @Override
        public boolean removeCache(String key, CacheInfo info)
        {
            mMMKV.remove(key);
            return true;
        }
    };
}
```
