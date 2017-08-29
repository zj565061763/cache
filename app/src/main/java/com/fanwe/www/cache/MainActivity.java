package com.fanwe.www.cache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        SDDiskCache.init(this);
        SDDiskCache.setGlobalObjectConverter(new JsonObjectConverter());

        TestModel model = new TestModel(); //创建实体
        SDDiskCache.open().putObject(model); //保存实体

        TestModel modelCached = SDDiskCache.open().getObject(TestModel.class); //查询保存的实体
        tv_info.setText(String.valueOf(modelCached));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete();
    }
}
