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

        SDDiskCache.init(this); //初始化
        SDDiskCache.setGlobalObjectConverter(new GlobalObjectConverter()); //设置全局对象转换器
        SDDiskCache.setGlobalEncryptConverter(new GlobalEncryptConverter()); //设置全局加解密转换器

        TestModel model = new TestModel(); //创建实体
        SDDiskCache.open().putObject(model, true); //保存实体，以加密方式保存

        TestModel modelCached = SDDiskCache.open().getObject(TestModel.class); //查询保存的实体
        tv_info.setText(String.valueOf(modelCached));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDDiskCache.open().delete(); //删除该目录对应的所有缓存
    }
}
