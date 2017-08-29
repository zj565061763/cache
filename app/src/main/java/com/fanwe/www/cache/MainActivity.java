package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.fanwe.library.cache.SDDiskCache;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private TextView tv_info;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_info = (TextView) findViewById(R.id.tv_info);

        SDDiskCache.init(this); //初始化
        SDDiskCache.setGlobalObjectConverter(new GlobalObjectConverter()); //设置全局对象转换器，必须设置
        SDDiskCache.setGlobalEncryptConverter(new GlobalEncryptConverter()); //设置全局加解密转换器，如果不需要加解密，可以不设置

        SDDiskCache.open().putObject(new TestModel());

        startThread1();
        startThread2();
    }

    private void startThread1()
    {
        new Thread("thread 1")
        {
            @Override
            public void run()
            {
                mRunnable1.run();
                super.run();
            }
        }.start();
    }

    private void startThread2()
    {
        new Thread("thread 2")
        {
            @Override
            public void run()
            {
                mRunnable2.run();
                super.run();
            }
        }.start();
    }

    private Runnable mRunnable1 = new Runnable()
    {
        @Override
        public void run()
        {
            for (int i = 0; i < 1000; i++)
            {
                Log.i(TAG, "put:" + i);

                TestModel model = SDDiskCache.open().getObject(TestModel.class);
                model.setValueInt(i);
                SDDiskCache.open().putObject(model);

                try
                {
                    Thread.sleep(1);
                } catch (Exception e)
                {
                }

                Log.i(TAG, "get-----:" + SDDiskCache.open().getObject(TestModel.class).getValueInt());
            }
        }
    };

    private Runnable mRunnable2 = new Runnable()
    {
        @Override
        public void run()
        {
            for (int i = 2000; i < 3000; i++)
            {
                Log.e(TAG, "put:" + i);

                TestModel model = SDDiskCache.open().getObject(TestModel.class);
                model.setValueInt(i);
                SDDiskCache.open().putObject(model);

                try
                {
                    Thread.sleep(1);
                } catch (Exception e)
                {
                }

                Log.e(TAG, "get-----:" + SDDiskCache.open().getObject(TestModel.class).getValueInt());
            }
        }
    };


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete(); //删除该目录对应的所有缓存
    }
}
