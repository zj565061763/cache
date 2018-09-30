# Gradle
[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

# 简单demo
```java
public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // 初始化
        FCache.init(new CacheConfig.Builder()
                // 如果需要加解密，设置全局加解密转换器
                .setEncryptConverter(new GlobalEncryptConverter())
                // 设置全局Gson对象转换器
                .setObjectConverter(new GsonObjectConverter())
                // 设置全局异常监听
                .setExceptionHandler(new Cache.ExceptionHandler()
                {
                    @Override
                    public void onException(Exception e)
                    {

                    }
                })
                .build(this)
        );
    }
}
```

```java
public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 使用内部存储
         *
         * 内部存储目录"/data/包名/files/disk_file"
         */
        FDisk.open();
        /**
         * 优先使用外部存储，如果外部存储不存在，则使用内部存储
         *
         * 外部存储目录"Android/data/包名/files/disk_file"
         */
        FDisk.openExternal();
        /**
         * 使用指定的目录
         */
        FDisk.openDir(Environment.getExternalStorageDirectory());

        putData();
        printData();
    }

    private void putData()
    {
        FDisk.open().cacheInteger().put(KEY, 1);
        FDisk.open().cacheLong().put(KEY, 22L);
        FDisk.open().cacheFloat().put(KEY, 333.333F);
        FDisk.open().cacheDouble().put(KEY, 4444.4444D);
        FDisk.open().cacheBoolean().put(KEY, true);
        FDisk.open().cacheString().put(KEY, "hello String");
        FDisk.open().cacheObject().put(new TestModel());
    }

    private void printData()
    {
        Log.i(TAG, "getInt:" + FDisk.open().cacheInteger().get(KEY));
        Log.i(TAG, "getLong:" + FDisk.open().cacheLong().get(KEY));
        Log.i(TAG, "getFloat:" + FDisk.open().cacheFloat().get(KEY));
        Log.i(TAG, "getDouble:" + FDisk.open().cacheDouble().get(KEY));
        Log.i(TAG, "getBoolean:" + FDisk.open().cacheBoolean().get(KEY));
        Log.i(TAG, "getString:" + FDisk.open().cacheString().get(KEY));
        Log.i(TAG, "getObject:" + FDisk.open().cacheObject().get(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // 删除该目录对应的所有缓存
        FDisk.open().delete();
    }
}
```

# 创建对象转换器
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

# 创建加解密转换器
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
