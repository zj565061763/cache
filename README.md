
## Gradle
[![](https://jitpack.io/v/zj565061763/cache.svg)](https://jitpack.io/#zj565061763/cache)

## 简单demo
```java
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private String key = "key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FDisk.init(this); //初始化

        /**
         * 当数据量较大的时候建议用cacheObject()方法，性能会比cacheSerializable()好很多
         * 如果要用cacheObject()方法，需要配置对象转换器
         */
        FDisk.setGlobalObjectConverter(new GsonObjectConverter());     //配置全局Gson对象转换器
        FDisk.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，配置全局加解密转换器

        //不同的open方法可以关联不同的目录
        FDisk.open();                    // 外部存储"Android/data/包名/files/disk_file"目录
        FDisk.open("hello");             // 外部存储"Android/data/包名/files/hello"目录
        FDisk.openCache();               // 外部存储"Android/data/包名/cache/disk_cache"目录
        FDisk.openCache("hello");        // 外部存储"Android/data/包名/cache/hello"目录

        FDisk.openInternal();             // 内部存储"/data/包名/files/disk_file"目录
        FDisk.openInternal("hello");      // 内部存储"/data/包名/files/hello"目录
        FDisk.openInternalCache();        // 内部存储"/data/包名/cache/disk_cache"目录
        FDisk.openInternalCache("hello"); // 内部存储"/data/包名/cache/hello"目录
        FDisk.openDir(Environment.getExternalStorageDirectory()); //关联指定的目录

        putData();
        printData();
    }

    private void putData()
    {
        FDisk.open().cacheInteger().put(key, 1);
        FDisk.open().cacheLong().put(key, 2L);
        FDisk.open().cacheFloat().put(key, 3.3F);
        FDisk.open().cacheDouble().put(key, 4.4444D);
        FDisk.open().cacheBoolean().put(key, true);
        FDisk.open().cacheString().put(key, "hello String");

        FDisk.open().cacheSerializable().put(new TestModel());
        FDisk.open().cacheObject().put(new TestModel());
    }

    private void printData()
    {
        Log.i(TAG, "getInt:" + FDisk.open().cacheInteger().get(key));
        Log.i(TAG, "getLong:" + FDisk.open().cacheLong().get(key));
        Log.i(TAG, "getFloat:" + FDisk.open().cacheFloat().get(key));
        Log.i(TAG, "getDouble:" + FDisk.open().cacheDouble().get(key));
        Log.i(TAG, "getBoolean:" + FDisk.open().cacheBoolean().get(key));
        Log.i(TAG, "getString:" + FDisk.open().cacheString().get(key));
        Log.i(TAG, "getSerializable:" + FDisk.open().cacheSerializable().get(TestModel.class));
        Log.i(TAG, "getObject:" + FDisk.open().cacheObject().get(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FDisk.open().delete(); //删除该目录对应的所有缓存
    }
}
```

## 创建对象转换器
```java
public class GsonObjectConverter implements IObjectConverter
{
    private static final Gson GSON = new Gson();

    @Override
    public String objectToString(Object object)
    {
        return GSON.toJson(object); //对象转json
    }

    @Override
    public <T> T stringToObject(String string, Class<T> clazz)
    {
        return GSON.fromJson(string, clazz); //json转对象
    }
}
```

## 创建加解密转换器
```java
public class GlobalEncryptConverter implements IEncryptConverter
{
    @Override
    public String encrypt(String content)
    {
        return AESUtil.encrypt(content); //加密
    }

    @Override
    public String decrypt(String content)
    {
        return AESUtil.decrypt(content); //解密
    }
}
```
