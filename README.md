
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
         * 当数据量较大的时候建议用XXXObject方法，性能会比XXXSerializable好非常多
         * 如果要用XXXObject方法，需要配置对象转换器
         *
         * 用魅族MX6测试，测试对象中的map有10000条数据，测试结果如下：
         *
         * Fastjson1.1.62版本对象转换器:
         * putObject在70毫秒左右
         * getObject在140毫秒左右
         *
         * Gson2.8.1版本对象转换器:
         * putObject在65毫秒左右
         * getObject在140毫秒左右
         *
         * putSerializable在600毫秒左右
         * getSerializable在700毫秒左右
         */
        FDisk.setGlobalObjectConverter(new FastjsonObjectConverter());//配置Fastjson对象转换器
//        FDisk.setGlobalObjectConverter(new GsonObjectConverter());//配置Gson对象转换器
        FDisk.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，需要配置加解密转换器

        //不同的open方法可以关联不同的目录
        FDisk.open();             //"Android/data/包名/files/disk_file"
        FDisk.open("hello");      //"Android/data/包名/files/hello"
        FDisk.openCache();        //"Android/data/包名/cache/disk_cache"
        FDisk.openCache("hello"); //"Android/data/包名/cache/hello"
        FDisk.openDir(Environment.getExternalStorageDirectory()); //关联指定的目录

        FDisk.open().putInt(key, 1);
        FDisk.open().putLong(key, 2);
        FDisk.open().putFloat(key, 3.3f);
        FDisk.open().putDouble(key, 4.4444d);
        FDisk.open().putBoolean(key, true);
        FDisk.open().putString(key, "hello String");
        FDisk.open().putSerializable(new TestModel());
        FDisk.open().setEncrypt(true).putObject(new TestModel()); //加密实体

        print();
    }

    private void print()
    {
        Log.i(TAG, "getInt:" + FDisk.open().getInt(key, 0));
        Log.i(TAG, "getLong:" + FDisk.open().getLong(key, 0));
        Log.i(TAG, "getFloat:" + FDisk.open().getFloat(key, 0));
        Log.i(TAG, "getDouble:" + FDisk.open().getDouble(key, 0));
        Log.i(TAG, "getBoolean:" + FDisk.open().getBoolean(key, false));
        Log.i(TAG, "getString:" + FDisk.open().getString(key));
        Log.i(TAG, "getSerializable:" + FDisk.open().getSerializable(TestModel.class));
        Log.i(TAG, "getObject:" + FDisk.open().getObject(TestModel.class));
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
public class GlobalObjectConverter implements IObjectConverter
{
    @Override
    public String objectToString(Object object)
    {
        return JSON.toJSONString(object); //对象转json
    }

    @Override
    public <T> T stringToObject(String string, Class<T> clazz)
    {
        return JSON.parseObject(string, clazz); //json转对象
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
