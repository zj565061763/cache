
## Gradle
`compile 'com.fanwe.android:cache:1.0.5'`

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

        SDDisk.init(this); //初始化

        /**
         * 当数据量较大的时候建议用XXXObject方法，性能会比XXXSerializable好非常多
         *
         * 用魅族MX6测试，测试对象中的map有10000条数据，对象转换器用FastJson实现，测试结果如下：
         *
         * putObject在10毫秒之内
         * getObject在50毫秒左右
         *
         * putSerializable在500毫秒左右
         * getSerializable在700毫秒左右
         */
        SDDisk.setGlobalObjectConverter(new GlobalObjectConverter());//如果要用XXXObject方法，需要配置Object对象转换器
        SDDisk.setGlobalEncryptConverter(new GlobalEncryptConverter()); //如果需要加解密，需要配置加解密转换器

        //不同的open方法可以关联不同的目录
        SDDisk.open();             //"Android/data/包名/files/disk_file"
        SDDisk.open("hello");      //"Android/data/包名/files/hello"
        SDDisk.openCache();        //"Android/data/包名/cache/disk_cache"
        SDDisk.openCache("hello"); //"Android/data/包名/cache/hello"
        SDDisk.openDir(Environment.getExternalStorageDirectory()); //关联指定的目录

        SDDisk.open().putInt(key, 1);
        SDDisk.open().putLong(key, 2);
        SDDisk.open().putFloat(key, 3.3f);
        SDDisk.open().putDouble(key, 4.4444d);
        SDDisk.open().putBoolean(key, true);
        SDDisk.open().putString(key, "hello String");
        SDDisk.open().putSerializable(new TestModel());
        SDDisk.open().setEncrypt(true).putObject(new TestModel()); //加密实体

        print();
    }

    private void print()
    {
        Log.i(TAG, "getInt:" + SDDisk.open().getInt(key, 0));
        Log.i(TAG, "getLong:" + SDDisk.open().getLong(key, 0));
        Log.i(TAG, "getFloat:" + SDDisk.open().getFloat(key, 0));
        Log.i(TAG, "getDouble:" + SDDisk.open().getDouble(key, 0));
        Log.i(TAG, "getBoolean:" + SDDisk.open().getBoolean(key, false));
        Log.i(TAG, "getString:" + SDDisk.open().getString(key));
        Log.i(TAG, "getSerializable:" + SDDisk.open().getSerializable(TestModel.class));
        Log.i(TAG, "getObject:" + SDDisk.open().getObject(TestModel.class));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDisk.open().delete(); //删除该目录对应的所有缓存
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
