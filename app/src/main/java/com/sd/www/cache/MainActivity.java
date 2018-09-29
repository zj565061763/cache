package com.sd.www.cache;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sd.lib.cache.FDisk;
import com.sd.www.cache.converter.GlobalEncryptConverter;
import com.sd.www.cache.converter.GsonObjectConverter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "MainActivity";

    private final String key = "key";
    private final TestModel mTestModel = new TestModel();

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
        FDisk.open().cacheInteger().put(key, 1);
        FDisk.open().cacheLong().put(key, 22L);
        FDisk.open().cacheFloat().put(key, 333.333F);
        FDisk.open().cacheDouble().put(key, 4444.4444D);
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
    public void onClick(View v)
    {
        SDTimeLogger.test("cacheSerializable put", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().cacheSerializable().put(mTestModel);
            }
        });

        SDTimeLogger.test("cacheSerializable get", new Runnable()
        {
            @Override
            public void run()
            {
                FDisk.open().cacheSerializable().get(TestModel.class);
            }
        });
    }
}
